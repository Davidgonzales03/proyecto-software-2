package bo.edu.uagrm.tienda.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.config.RetiroProperties;
import bo.edu.uagrm.tienda.dto.CancelacionResponse;
import bo.edu.uagrm.tienda.dto.ConfirmacionRequest;
import bo.edu.uagrm.tienda.dto.EstadoLineaCarrito;
import bo.edu.uagrm.tienda.dto.PedidoResponse;
import bo.edu.uagrm.tienda.dto.RetiroResponse;
import bo.edu.uagrm.tienda.entity.EstadoPedido;
import bo.edu.uagrm.tienda.entity.LineaCarrito;
import bo.edu.uagrm.tienda.entity.LineaPedido;
import bo.edu.uagrm.tienda.entity.MovimientoStock;
import bo.edu.uagrm.tienda.entity.Pedido;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.exception.CarritoVacioException;
import bo.edu.uagrm.tienda.exception.PedidoNoEncontradoException;
import bo.edu.uagrm.tienda.exception.PedidoNoPendienteException;
import bo.edu.uagrm.tienda.exception.PedidoPendienteException;
import bo.edu.uagrm.tienda.exception.StockInsuficienteException;
import bo.edu.uagrm.tienda.exception.StockInsuficienteException.LineaRechazada;
import bo.edu.uagrm.tienda.exception.StockInsuficienteException.Motivo;
import bo.edu.uagrm.tienda.exception.TotalCambiadoException;
import bo.edu.uagrm.tienda.repository.LineaCarritoRepository;
import bo.edu.uagrm.tienda.repository.MovimientoStockRepository;
import bo.edu.uagrm.tienda.repository.PedidoRepository;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

// Los pedidos del Cliente (HU-12; decisiones.md → Pedidos). Las filas se bloquean siempre en el mismo orden: el
// cliente, después el pedido y después los productos por id ascendente. Así dos confirmaciones, una cancelación, el
// vencimiento y el inventario nunca se esperan en círculo (RF-7, RF-12). Cada fila se bloquea antes de leerla en la
// transacción: una entidad ya leída no se vuelve a leer al bloquearla y quedaría con datos viejos
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(RetiroProperties.class)
public class PedidoService {

	public static final Duration VIGENCIA = Duration.ofMinutes(30);

	private final PedidoRepository pedidoRepository;
	private final LineaCarritoRepository lineaCarritoRepository;
	private final ProductoRepository productoRepository;
	private final UsuarioRepository usuarioRepository;
	private final MovimientoStockRepository movimientoRepository;
	private final RetiroProperties retiroProperties;
	private final Clock clock;

	public RetiroResponse retiro() {
		return RetiroResponse.de(retiroProperties);
	}

	// Todo o nada, en una sola transacción (RF-2, RF-3)
	@Transactional
	public PedidoResponse confirmar(Long idUsuario, ConfirmacionRequest solicitud) {
		Usuario cliente = bloquearCliente(idUsuario);
		LocalDateTime ahora = LocalDateTime.now(clock);
		// Uno vencido no bloquea (RF-5): vence acá mismo y sus líneas vuelven al carrito antes de leerlo. Se leen sin
		// bloquearlos: todo lo que cambia un pedido bloquea antes al cliente, y en MySQL un SELECT … FOR UPDATE sin
		// filas bloquea el hueco del índice y traba las confirmaciones de dos clientes a la vez
		List<Pedido> vencidos = new ArrayList<>();
		for (Pedido pendiente : pedidoRepository.findByUsuario_IdUsuarioAndEstadoOrderByIdPedido(idUsuario,
				EstadoPedido.PENDIENTE_PAGO)) {
			if (pendiente.getVence().isAfter(ahora)) {
				throw new PedidoPendienteException(pendiente.getIdPedido());
			}
			vencidos.add(pendiente);
		}
		// Los productos del carrito y los de lo que vence, todos juntos por id ascendente, y antes de leer el
		// carrito, que los trae con su stock
		SortedSet<Long> idsProductos = new TreeSet<>(lineaCarritoRepository.idsDeProductos(idUsuario));
		vencidos.forEach(vencido -> vencido.getLineas().forEach(linea -> idsProductos.add(linea.getIdProducto())));
		idsProductos.forEach(this::bloquearProducto);
		vencidos.forEach(vencido -> liberar(vencido, EstadoPedido.VENCIDO, ahora));
		List<LineaCarrito> lineas = lineaCarritoRepository.findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(
				idUsuario);
		if (lineas.isEmpty()) {
			throw new CarritoVacioException(); // RF-4
		}
		List<LineaRechazada> rechazadas = lineas.stream().map(PedidoService::rechazo).flatMap(Optional::stream)
				.toList();
		if (!rechazadas.isEmpty()) {
			throw new StockInsuficienteException(rechazadas);
		}
		Pedido pedido = Pedido.confirmar(cliente, solicitud.nombreRetira(), solicitud.telefonoRetira(),
				solicitud.nota(), ahora, ahora.plus(VIGENCIA));
		lineas.forEach(linea -> pedido.agregarLinea(linea.getProducto(), linea.getCantidad()));
		// Con los precios del servidor; el del cliente solo se compara (RF-15)
		if (pedido.getTotal().compareTo(solicitud.totalEsperado()) != 0) {
			throw new TotalCambiadoException(pedido.getTotal());
		}
		pedidoRepository.save(pedido);
		for (LineaPedido linea : pedido.getLineas()) {
			Producto producto = linea.getProducto();
			// Nunca negativo: se revisó con el producto bloqueado (RF-14)
			BigDecimal nuevo = producto.getStock().subtract(linea.getCantidad());
			producto.aplicarMovimiento(nuevo);
			movimientoRepository.save(MovimientoStock.reserva(producto, linea.getCantidad(), nuevo, pedido, ahora));
		}
		lineaCarritoRepository.deleteByUsuario_IdUsuario(idUsuario);
		return PedidoResponse.from(pedido, retiro(), ahora);
	}

	// Primero vence el del cliente si ya pasó la hora: nunca ve «Pendiente» con el tiempo agotado (RF-10)
	@Transactional
	public PedidoResponse ver(Long idUsuario, Long idPedido) {
		LocalDateTime ahora = LocalDateTime.now(clock);
		vencerSiCorresponde(idUsuario);
		Pedido pedido = pedidoRepository.findByIdPedidoAndUsuario_IdUsuario(idPedido, idUsuario)
				.orElseThrow(PedidoNoEncontradoException::new);
		return PedidoResponse.from(pedido, retiro(), ahora);
	}

	@Transactional
	public CancelacionResponse cancelar(Long idUsuario, Long idPedido) {
		Pedido pedido = bloquear(idUsuario, idPedido);
		if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
			throw new PedidoNoPendienteException(); // RF-12
		}
		LocalDateTime ahora = LocalDateTime.now(clock);
		int noVolvieron = liberar(pedido, EstadoPedido.CANCELADO, ahora);
		return new CancelacionResponse(PedidoResponse.from(pedido, retiro(), ahora), noVolvieron);
	}

	// Para la tarea programada (VencimientoPedidos), un pedido por transacción. Si ya se canceló o venció, o todavía
	// no es la hora, no hace nada (RF-12)
	@Transactional
	public void vencer(Long idUsuario, Long idPedido) {
		Pedido pedido = bloquear(idUsuario, idPedido);
		LocalDateTime ahora = LocalDateTime.now(clock);
		if (pedido.getEstado() == EstadoPedido.PENDIENTE_PAGO && !pedido.getVence().isAfter(ahora)) {
			liberar(pedido, EstadoPedido.VENCIDO, ahora);
		}
	}

	// Al leer el carrito o un pedido. Sin nada vencido no bloquea nada: es lo de casi siempre
	@Transactional
	public void vencerSiCorresponde(Long idUsuario) {
		LocalDateTime ahora = LocalDateTime.now(clock);
		if (pedidoRepository.existsByUsuario_IdUsuarioAndEstadoAndVenceLessThanEqual(idUsuario,
				EstadoPedido.PENDIENTE_PAGO, ahora)) {
			bloquearCliente(idUsuario);
			vencerLosDelCliente(idUsuario, ahora);
		}
	}

	// Uno que ya pasó la hora no cuenta, aunque todavía no se haya vencido: las escrituras del carrito no vencen
	@Transactional(readOnly = true)
	public Optional<Long> idDelPendiente(Long idUsuario) {
		return pedidoRepository.findFirstByUsuario_IdUsuarioAndEstadoAndVenceAfter(idUsuario,
				EstadoPedido.PENDIENTE_PAGO, LocalDateTime.now(clock)).map(Pedido::getIdPedido);
	}

	// Con el cliente ya bloqueado. Cada pedido libera sus productos por id ascendente
	private void vencerLosDelCliente(Long idUsuario, LocalDateTime ahora) {
		for (Pedido pedido : pendientesBloqueados(idUsuario)) {
			if (!pedido.getVence().isAfter(ahora)) {
				liberar(pedido, EstadoPedido.VENCIDO, ahora);
			}
		}
	}

	// Uno como mucho (RF-5); la lista no lo da por supuesto
	private List<Pedido> pendientesBloqueados(Long idUsuario) {
		return pedidoRepository.findConBloqueoByUsuario_IdUsuarioAndEstadoOrderByIdPedido(idUsuario,
				EstadoPedido.PENDIENTE_PAGO);
	}

	// Cliente y después pedido: el mismo orden que confirmar. De otro cliente o inexistente, 404 (RF-13)
	private Pedido bloquear(Long idUsuario, Long idPedido) {
		usuarioRepository.findConBloqueoByIdUsuario(idUsuario).orElseThrow(PedidoNoEncontradoException::new);
		return pedidoRepository.findConBloqueoByIdPedidoAndUsuario_IdUsuario(idPedido, idUsuario)
				.orElseThrow(PedidoNoEncontradoException::new);
	}

	private Usuario bloquearCliente(Long idUsuario) {
		return usuarioRepository.findConBloqueoByIdUsuario(idUsuario).orElseThrow(PedidoNoEncontradoException::new);
	}

	private Producto bloquearProducto(Long idProducto) {
		return productoRepository.findConBloqueoByIdProducto(idProducto).orElseThrow();
	}

	// Devuelve el stock con los productos bloqueados por id ascendente, cierra el pedido y devuelve las líneas al
	// carrito (RF-9, RF-10, RF-11). Si las entradas de mientras tanto dejaron el stock cerca del máximo, queda en el
	// máximo: pasarlo haría fallar el guardado y el pedido no se cerraría nunca. Cuenta las líneas que no volvieron
	// (RF-16)
	private int liberar(Pedido pedido, EstadoPedido estadoFinal, LocalDateTime ahora) {
		for (LineaPedido linea : pedido.lineasPorProducto()) {
			Producto producto = bloquearProducto(linea.getIdProducto());
			// El máximo con los decimales que admite la unidad: por unidad, 9 999 999
			BigDecimal tope = InventarioService.STOCK_MAXIMO.setScale(producto.getUnidad().decimales(), RoundingMode.FLOOR);
			BigDecimal nuevo = producto.getStock().add(linea.getCantidad()).min(tope);
			BigDecimal devuelto = nuevo.subtract(producto.getStock());
			if (devuelto.signum() > 0) {
				producto.aplicarMovimiento(nuevo);
				movimientoRepository.save(MovimientoStock.liberacion(producto, devuelto, nuevo, pedido, ahora));
			}
		}
		if (estadoFinal == EstadoPedido.CANCELADO) {
			pedido.cancelar(ahora);
		} else {
			pedido.vencer(ahora);
		}
		return devolverAlCarrito(pedido, ahora);
	}

	// Se suman a las que ya estén, con los límites del carrito: la línea queda en 999 como mucho y, con 50 líneas,
	// las que faltan no vuelven (RF-11, RF-16). Un producto que ya no se vende vuelve igual y el carrito lo marca
	private int devolverAlCarrito(Pedido pedido, LocalDateTime ahora) {
		// El carrito se lee una vez: con el cliente bloqueado, nadie más lo cambia
		Map<Long, LineaCarrito> carrito = new HashMap<>();
		lineaCarritoRepository.findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(
				pedido.getUsuario().getIdUsuario())
				.forEach(linea -> carrito.put(linea.getProducto().getIdProducto(), linea));
		int noVolvieron = 0;
		for (LineaPedido linea : pedido.getLineas()) {
			LineaCarrito existente = carrito.get(linea.getIdProducto());
			if (existente != null) {
				existente.cambiar(existente.getCantidad().add(linea.getCantidad()).min(CarritoService.MAXIMO_CANTIDAD));
			} else if (carrito.size() < CarritoService.MAXIMO_LINEAS) {
				carrito.put(linea.getIdProducto(), lineaCarritoRepository.save(
						LineaCarrito.crear(pedido.getUsuario(), linea.getProducto(), linea.getCantidad(), ahora)));
			} else {
				noVolvieron++;
			}
		}
		return noVolvieron;
	}

	// Lo mismo que marca el carrito, y además si pide más de lo que hay (RF-3)
	private static Optional<LineaRechazada> rechazo(LineaCarrito linea) {
		Producto producto = linea.getProducto();
		EstadoLineaCarrito estado = CarritoService.estado(producto, linea.getCantidad());
		if (estado != EstadoLineaCarrito.DISPONIBLE) {
			return Optional.of(new LineaRechazada(producto.getIdProducto(), Motivo.valueOf(estado.name())));
		}
		if (linea.getCantidad().compareTo(producto.getStock()) > 0) {
			return Optional.of(new LineaRechazada(producto.getIdProducto(), Motivo.NO_ALCANZA));
		}
		return Optional.empty();
	}
}
