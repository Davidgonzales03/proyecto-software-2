package bo.edu.uagrm.tienda.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.dto.CantidadRequest;
import bo.edu.uagrm.tienda.dto.CarritoResponse;
import bo.edu.uagrm.tienda.dto.EstadoLineaCarrito;
import bo.edu.uagrm.tienda.dto.LineaCarritoRequest;
import bo.edu.uagrm.tienda.dto.LineaCarritoResponse;
import bo.edu.uagrm.tienda.dto.validacion.CantidadDeCarrito;
import bo.edu.uagrm.tienda.entity.LineaCarrito;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;
import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException.CampoInvalido;
import bo.edu.uagrm.tienda.exception.CarritoLlenoException;
import bo.edu.uagrm.tienda.exception.ProductoAgotadoException;
import bo.edu.uagrm.tienda.exception.ProductoNoEncontradoException;
import bo.edu.uagrm.tienda.exception.UsuarioNoEncontradoException;
import bo.edu.uagrm.tienda.repository.LineaCarritoRepository;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

// El carrito del Cliente (HU-11; decisiones.md → Carrito). Cada escritura bloquea primero la fila del cliente: dos
// pestañas a la vez se aplican una después de la otra. Las respuestas se arman con el precio y la disponibilidad de hoy,
// y nunca llevan el stock (RF-14)
@Service
@RequiredArgsConstructor
public class CarritoService {

	static final int MAXIMO_LINEAS = 50;
	static final BigDecimal MAXIMO_CANTIDAD = new BigDecimal(CantidadDeCarrito.MAXIMA);

	private final LineaCarritoRepository lineaRepository;
	private final ProductoRepository productoRepository;
	private final UsuarioRepository usuarioRepository;
	private final Clock clock;

	@Transactional(readOnly = true)
	public CarritoResponse ver(Long idUsuario) {
		return armar(idUsuario);
	}

	@Transactional
	public CarritoResponse agregar(Long idUsuario, LineaCarritoRequest solicitud) {
		// Con la fila del cliente bloqueada, dos agregados a la vez se aplican uno después del otro: ninguna suma se
		// pierde y el límite de 50 líneas se cuenta sobre lo que dejó el anterior (RF-2, RF-5). La restricción única
		// de la tabla queda como segunda defensa
		Usuario cliente = bloquear(idUsuario);
		Producto producto = productoRepository.publico(solicitud.idProducto())
				.orElseThrow(ProductoNoEncontradoException::new);
		if (producto.getStock().signum() == 0) {
			throw new ProductoAgotadoException();
		}
		Optional<LineaCarrito> existente = lineaRepository
				.findByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, producto.getIdProducto());
		BigDecimal cantidad = existente.map(linea -> linea.getCantidad().add(solicitud.cantidad()))
				.orElse(solicitud.cantidad());
		// La cantidad pedida ya pasó la solicitud: si se pasa, es por lo que ya estaba (RF-2, RF-3)
		if (existente.isPresent() && cantidad.compareTo(MAXIMO_CANTIDAD) > 0) {
			throw new CamposInvalidosException(List.of(new CampoInvalido("cantidad",
					"sumada a la del carrito pasaría de " + CantidadDeCarrito.MAXIMA)));
		}
		exigirCantidad(producto.getUnidad(), cantidad);
		if (existente.isPresent()) {
			existente.get().cambiar(cantidad);
		} else {
			if (lineaRepository.countByUsuario_IdUsuario(idUsuario) >= MAXIMO_LINEAS) {
				throw new CarritoLlenoException();
			}
			lineaRepository.save(LineaCarrito.crear(cliente, producto, cantidad, LocalDateTime.now(clock)));
		}
		return armar(idUsuario);
	}

	// Se puede cambiar aunque el producto ya no esté disponible: así se corrige una cantidad que la unidad dejó de
	// admitir (RF-10)
	@Transactional
	public CarritoResponse cambiarCantidad(Long idUsuario, Long idProducto, CantidadRequest solicitud) {
		bloquear(idUsuario);
		LineaCarrito linea = lineaRepository.findByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto)
				.orElseThrow(ProductoNoEncontradoException::new);
		exigirCantidad(linea.getProducto().getUnidad(), solicitud.cantidad());
		linea.cambiar(solicitud.cantidad());
		return armar(idUsuario);
	}

	// Quitar algo que no está no cambia nada (RF-11)
	@Transactional
	public CarritoResponse quitar(Long idUsuario, Long idProducto) {
		bloquear(idUsuario);
		lineaRepository.findByUsuario_IdUsuarioAndProducto_IdProducto(idUsuario, idProducto)
				.ifPresent(lineaRepository::delete);
		return armar(idUsuario);
	}

	@Transactional
	public CarritoResponse vaciar(Long idUsuario) {
		bloquear(idUsuario);
		lineaRepository.deleteByUsuario_IdUsuario(idUsuario);
		return armar(idUsuario);
	}

	private Usuario bloquear(Long idUsuario) {
		return usuarioRepository.findConBloqueoByIdUsuario(idUsuario).orElseThrow(UsuarioNoEncontradoException::new);
	}

	// Los decimales según la unidad y el tope de la línea, también para el resultado de una suma (RF-3). Que sea mayor
	// que 0 y tenga hasta dos decimales ya lo validó la solicitud
	private static void exigirCantidad(UnidadVenta unidad, BigDecimal cantidad) {
		if (!InventarioService.admiteDecimales(unidad, cantidad)) {
			throw new CamposInvalidosException(
					List.of(new CampoInvalido("cantidad", InventarioService.mensajeDecimales(unidad))));
		}
		if (cantidad.compareTo(MAXIMO_CANTIDAD) > 0) {
			throw new CamposInvalidosException(
					List.of(new CampoInvalido("cantidad", "admite hasta " + CantidadDeCarrito.MAXIMA)));
		}
	}

	private CarritoResponse armar(Long idUsuario) {
		List<LineaCarritoResponse> lineas = lineaRepository
				.findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(idUsuario).stream()
				.map(CarritoService::linea).toList();
		BigDecimal total = lineas.stream().map(LineaCarritoResponse::subtotal).filter(subtotal -> subtotal != null)
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
		return new CarritoResponse(lineas, total, lineas.size());
	}

	// Lee la categoría y las imágenes del producto: dentro de la transacción
	private static LineaCarritoResponse linea(LineaCarrito linea) {
		Producto producto = linea.getProducto();
		EstadoLineaCarrito estado = estado(producto, linea.getCantidad());
		BigDecimal subtotal = estado == EstadoLineaCarrito.DISPONIBLE
				? producto.getPrecio().multiply(linea.getCantidad()).setScale(2, RoundingMode.HALF_UP)
				: null;
		return new LineaCarritoResponse(producto.getIdProducto(), producto.getNombre(),
				producto.getImagenes().isEmpty() ? null : producto.getImagenes().getFirst(), producto.getPrecio(),
				producto.getUnidad(), linea.getCantidad(), subtotal, estado);
	}

	// Lo que dejó de venderse pesa más que lo agotado: un producto inactivo y sin stock dice «No disponible» (RF-9)
	private static EstadoLineaCarrito estado(Producto producto, BigDecimal cantidad) {
		if (!producto.isActivo() || !producto.getCategoria().isVisible()) {
			return EstadoLineaCarrito.NO_DISPONIBLE;
		}
		if (producto.getStock().signum() == 0) {
			return EstadoLineaCarrito.AGOTADO;
		}
		if (!InventarioService.admiteDecimales(producto.getUnidad(), cantidad)) {
			return EstadoLineaCarrito.CANTIDAD_INVALIDA;
		}
		return EstadoLineaCarrito.DISPONIBLE;
	}
}
