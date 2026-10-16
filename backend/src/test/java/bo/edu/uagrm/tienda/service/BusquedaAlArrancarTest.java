package bo.edu.uagrm.tienda.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import bo.edu.uagrm.tienda.entity.Categoria;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;
import bo.edu.uagrm.tienda.repository.ProductoRepository;

// HU-09 RF-8: los productos de antes de HU-09 no tienen el texto de búsqueda
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BusquedaAlArrancar.class)
class BusquedaAlArrancarTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private BusquedaAlArrancar busquedaAlArrancar;

	// HU-09 RF-8
	@Test
	void completaLaBusquedaDeLosProductosQueNoLaTienen() {
		Categoria jardin = em.persist(Categoria.crear("Jardín", null));
		Producto regadera = Producto.crear("Regadera Metálica", "Tramontína", null, new BigDecimal("35.00"),
				UnidadVenta.UNIDAD, jardin, List.of(), LocalDateTime.of(2026, 10, 10, 9, 0));
		Long id = em.persistAndGetId(regadera, Long.class);
		regadera.asignarCodigo("JAR-0001");
		em.flush();
		em.getEntityManager().createNativeQuery("update producto set busqueda = null").executeUpdate();
		em.clear();

		busquedaAlArrancar.completar();
		em.flush();
		em.clear();

		assertThat(productoRepository.findById(id).orElseThrow().getBusqueda())
				.isEqualTo("regadera metalica tramontina jar-0001");
	}
}
