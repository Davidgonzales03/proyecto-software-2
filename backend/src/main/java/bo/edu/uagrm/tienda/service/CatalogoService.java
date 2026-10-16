package bo.edu.uagrm.tienda.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.dto.OrdenCatalogo;
import bo.edu.uagrm.tienda.dto.PaginaResponse;
import bo.edu.uagrm.tienda.dto.PortadaResponse;
import bo.edu.uagrm.tienda.dto.ProductoTarjetaResponse;
import bo.edu.uagrm.tienda.dto.SeccionResponse;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException;
import bo.edu.uagrm.tienda.exception.CamposInvalidosException.CampoInvalido;
import bo.edu.uagrm.tienda.repository.CategoriaRepository;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;

// El catálogo público (HU-09; decisiones.md → Catálogo público): solo productos activos de categorías visibles, y
// nunca la cantidad en stock
@Service
@RequiredArgsConstructor
public class CatalogoService {

	static final int POR_PAGINA = 24;
	static final int POR_CARRUSEL = 12;
	static final int BUSQUEDA_MAXIMA = 100;
	// La última página posible: más allá, el desplazamiento no entra en un int
	private static final int PAGINA_MAXIMA = Integer.MAX_VALUE / POR_PAGINA - 1;
	private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999.99");

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;

	public record Filtros(String texto, Long idCategoria, String marca, BigDecimal precioMin, BigDecimal precioMax,
			boolean soloDisponibles, OrdenCatalogo orden, int pagina) {
	}

	@Transactional(readOnly = true)
	public PaginaResponse<ProductoTarjetaResponse> listar(Filtros filtros) {
		validar(filtros);
		String marca = filtros.marca() == null || filtros.marca().isBlank() ? null
				: filtros.marca().strip().toLowerCase(Locale.ROOT);
		OrdenCatalogo orden = filtros.orden() == null ? OrdenCatalogo.RECIENTES : filtros.orden();
		// Una página negativa es la primera, como en la gestión (HU-07); una enorme, una que llega vacía
		PageRequest paginacion = PageRequest.of(Math.clamp(filtros.pagina(), 0, PAGINA_MAXIMA), POR_PAGINA,
				orden.orden());
		return PaginaResponse.from(productoRepository.catalogo(patronDeBusqueda(filtros.texto()), filtros.idCategoria(),
				marca, filtros.precioMin(), filtros.precioMax(), filtros.soloDisponibles(), paginacion),
				ProductoTarjetaResponse::from);
	}

	@Transactional(readOnly = true)
	public PortadaResponse portada() {
		List<SeccionResponse> categorias = new ArrayList<>();
		categoriaRepository.findByVisibleTrueOrderByNombreClave().forEach(categoria -> {
			List<ProductoTarjetaResponse> productos = vitrina(categoria.getIdCategoria(), false);
			if (!productos.isEmpty()) {
				categorias.add(new SeccionResponse(categoria.getIdCategoria(), categoria.getNombre(), productos));
			}
		});
		return new PortadaResponse(vitrina(null, true), vitrina(null, false), categorias);
	}

	@Transactional(readOnly = true)
	public List<String> marcas() {
		return productoRepository.marcasDelCatalogo();
	}

	private List<ProductoTarjetaResponse> vitrina(Long idCategoria, boolean soloDestacados) {
		return productoRepository
				.vitrina(idCategoria, soloDestacados, PageRequest.of(0, POR_CARRUSEL, OrdenCatalogo.RECIENTES.orden()))
				.stream().map(ProductoTarjetaResponse::from).toList();
	}

	// HU-09 RF-13, RF-14: lo que la pantalla ya descarta, la API lo rechaza
	private static void validar(Filtros filtros) {
		List<CampoInvalido> errores = new ArrayList<>();
		if (filtros.texto() != null && filtros.texto().strip().length() > BUSQUEDA_MAXIMA) {
			errores.add(new CampoInvalido("q", "admite hasta " + BUSQUEDA_MAXIMA + " caracteres"));
		}
		validarPrecio("precioMin", filtros.precioMin(), errores);
		validarPrecio("precioMax", filtros.precioMax(), errores);
		if (errores.isEmpty() && filtros.precioMin() != null && filtros.precioMax() != null
				&& filtros.precioMin().compareTo(filtros.precioMax()) > 0) {
			errores.add(new CampoInvalido("precioMax", "tiene que ser mayor o igual que el mínimo"));
		}
		if (!errores.isEmpty()) {
			throw new CamposInvalidosException(errores);
		}
	}

	private static void validarPrecio(String campo, BigDecimal precio, List<CampoInvalido> errores) {
		if (precio == null) {
			return;
		}
		if (precio.signum() < 0 || precio.compareTo(PRECIO_MAXIMO) > 0) {
			errores.add(new CampoInvalido(campo, "tiene que estar entre 0 y 99 999,99"));
		} else if (precio.stripTrailingZeros().scale() > 2) {
			errores.add(new CampoInvalido(campo, "admite hasta dos decimales"));
		}
	}

	// Como en la gestión, pero sobre el texto ya sin tildes del producto (HU-09 RF-8)
	private static String patronDeBusqueda(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		// '!' primero, para no volver a escapar los que agregan las otras dos sustituciones
		String literal = Producto.textoDeBusqueda(texto).replace("!", "!!").replace("%", "!%").replace("_", "!_");
		return "%" + literal + "%";
	}
}
