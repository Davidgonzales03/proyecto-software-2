package bo.edu.uagrm.tienda.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.dto.AjusteRequest;
import bo.edu.uagrm.tienda.dto.EntradaRequest;
import bo.edu.uagrm.tienda.dto.InventarioResponse;
import bo.edu.uagrm.tienda.dto.MinimoRequest;
import bo.edu.uagrm.tienda.dto.MovimientoResponse;
import bo.edu.uagrm.tienda.entity.MotivoAjuste;
import bo.edu.uagrm.tienda.entity.MovimientoStock;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;
import bo.edu.uagrm.tienda.exception.AjusteSinCambioException;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException.CampoInvalido;
import bo.edu.uagrm.tienda.exception.ProductoNoEncontradoException;
import bo.edu.uagrm.tienda.exception.StockFueraDeRangoException;
import bo.edu.uagrm.tienda.repository.MovimientoStockRepository;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

// Stock y movimientos (HU-08; decisiones.md → Inventario). Cada movimiento bloquea la fila del producto antes de
// leer el stock: dos a la vez se aplican uno después del otro y ninguno se pierde (RF-8)
@Service
@RequiredArgsConstructor
public class InventarioService {

	static final BigDecimal STOCK_MAXIMO = new BigDecimal("9999999.99");
	private static final int TAMANO_POR_DEFECTO = 20;
	private static final int TAMANO_MAXIMO = 100;
	private static final Sort ORDEN_INVENTARIO = Sort.by("nombre", "idProducto");
	private static final Sort ORDEN_HISTORIAL = Sort.by(Sort.Direction.DESC, "fecha", "idMovimiento");

	private final ProductoRepository productoRepository;
	private final MovimientoStockRepository movimientoRepository;
	private final UsuarioRepository usuarioRepository;
	private final Clock clock;

	@Transactional(readOnly = true)
	public Page<InventarioResponse> listar(String texto, Long idCategoria, Boolean activo, boolean soloBajo,
			int pagina, int tamano) {
		return productoRepository.inventario(patronDeBusqueda(texto), idCategoria, activo, soloBajo,
				paginacion(pagina, tamano, ORDEN_INVENTARIO)).map(InventarioResponse::from);
	}

	@Transactional(readOnly = true)
	public InventarioResponse obtener(Long idProducto) {
		return InventarioResponse.from(
				productoRepository.findById(idProducto).orElseThrow(ProductoNoEncontradoException::new));
	}

	@Transactional(readOnly = true)
	public Page<MovimientoResponse> historial(Long idProducto, int pagina, int tamano) {
		if (!productoRepository.existsById(idProducto)) {
			throw new ProductoNoEncontradoException();
		}
		return movimientoRepository.findByProducto_IdProducto(idProducto, paginacion(pagina, tamano, ORDEN_HISTORIAL))
				.map(MovimientoResponse::from);
	}

	@Transactional
	public InventarioResponse registrarEntrada(Long idProducto, Long idUsuario, EntradaRequest solicitud) {
		Producto producto = bloquear(idProducto);
		exigirDecimales(producto.getUnidad(), solicitud.cantidad(), "cantidad");
		BigDecimal nuevo = producto.getStock().add(solicitud.cantidad());
		if (nuevo.compareTo(STOCK_MAXIMO) > 0) {
			throw new StockFueraDeRangoException();
		}
		producto.aplicarMovimiento(nuevo);
		movimientoRepository.save(MovimientoStock.entrada(producto, solicitud.cantidad(), nuevo,
				usuarioRepository.getReferenceById(idUsuario), LocalDateTime.now(clock), solicitud.nota()));
		return InventarioResponse.from(producto);
	}

	// Se escribe lo contado; el movimiento guarda la diferencia
	@Transactional
	public InventarioResponse registrarAjuste(Long idProducto, Long idUsuario, AjusteRequest solicitud) {
		Producto producto = bloquear(idProducto);
		exigirDecimales(producto.getUnidad(), solicitud.contada(), "contada");
		if (solicitud.motivo() == MotivoAjuste.OTRO && (solicitud.nota() == null || solicitud.nota().isBlank())) {
			throw new CamposInvalidosException(
					List.of(new CampoInvalido("nota", "es obligatoria con el motivo «otro»")));
		}
		BigDecimal diferencia = solicitud.contada().subtract(producto.getStock());
		if (diferencia.signum() == 0) {
			throw new AjusteSinCambioException();
		}
		producto.aplicarMovimiento(solicitud.contada());
		movimientoRepository.save(MovimientoStock.ajuste(producto, diferencia, solicitud.contada(), solicitud.motivo(),
				usuarioRepository.getReferenceById(idUsuario), LocalDateTime.now(clock), solicitud.nota()));
		return InventarioResponse.from(producto);
	}

	@Transactional
	public InventarioResponse cambiarMinimo(Long idProducto, MinimoRequest solicitud) {
		Producto producto = bloquear(idProducto);
		if (solicitud.minimo() != null) {
			exigirDecimales(producto.getUnidad(), solicitud.minimo(), "minimo");
		}
		producto.cambiarMinimo(solicitud.minimo());
		return InventarioResponse.from(producto);
	}

	// «2,00» en una unidad entera es 2: cuentan los decimales que no son cero
	static boolean admiteDecimales(UnidadVenta unidad, BigDecimal cantidad) {
		return cantidad.stripTrailingZeros().scale() <= unidad.decimales();
	}

	private static void exigirDecimales(UnidadVenta unidad, BigDecimal cantidad, String campo) {
		if (!admiteDecimales(unidad, cantidad)) {
			String mensaje = unidad.decimales() == 0 ? "tiene que ser un número entero" : "admite hasta dos decimales";
			throw new CamposInvalidosException(List.of(new CampoInvalido(campo, mensaje)));
		}
	}

	private Producto bloquear(Long idProducto) {
		return productoRepository.findConBloqueoByIdProducto(idProducto)
				.orElseThrow(ProductoNoEncontradoException::new);
	}

	// Igual que el Pageable de Spring Data: una página negativa es la primera y un tamaño inválido, el por defecto
	private static PageRequest paginacion(int pagina, int tamano, Sort orden) {
		return PageRequest.of(Math.max(pagina, 0), tamano < 1 ? TAMANO_POR_DEFECTO : Math.min(tamano, TAMANO_MAXIMO),
				orden);
	}

	private static String patronDeBusqueda(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		// '!' primero, para no volver a escapar los que agregan las otras dos sustituciones
		String literal = texto.strip().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
		return "%" + literal + "%";
	}
}
