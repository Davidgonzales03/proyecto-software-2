package bo.edu.uagrm.tienda.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

// HU-09: lo que el catálogo necesita del producto
class ProductoTest {

	private static final Categoria JARDIN = Categoria.crear("Jardín", null);

	private static Producto producto(String nombre, String marca) {
		return Producto.crear(nombre, marca, null, new BigDecimal("85.00"), UnidadVenta.UNIDAD, JARDIN, List.of(),
				LocalDateTime.of(2026, 10, 16, 9, 0));
	}

	// HU-09 RF-4
	@Test
	void productoNuevoNoEsDestacado() {
		assertThat(producto("Regadera", null).isDestacado()).isFalse();
	}

	// HU-09 RF-4
	@Test
	void destacadoSeMarcaYSeDesmarca() {
		Producto regadera = producto("Regadera", null);

		regadera.marcarDestacado(true);
		assertThat(regadera.isDestacado()).isTrue();

		regadera.marcarDestacado(false);
		assertThat(regadera.isDestacado()).isFalse();
	}

	// HU-09 RF-8
	@Test
	void busquedaGuardaNombreMarcaYCodigoSinTildesNiMayusculas() {
		Producto martillo = producto("  Martillo  de Carpintería ", "TRÚPER");
		assertThat(martillo.getBusqueda()).isEqualTo("martillo de carpinteria truper");

		martillo.asignarCodigo("JAR-0007");

		assertThat(martillo.getBusqueda()).isEqualTo("martillo de carpinteria truper jar-0007");
	}

	// HU-09 RF-8
	@Test
	void busquedaSeRecalculaAlEditar() {
		Producto martillo = producto("Martillo", "Truper");
		martillo.asignarCodigo("JAR-0007");

		martillo.editar("Mazo de goma", null, null, new BigDecimal("40.00"), UnidadVenta.UNIDAD, JARDIN, List.of());

		assertThat(martillo.getBusqueda()).isEqualTo("mazo de goma jar-0007");
	}
}
