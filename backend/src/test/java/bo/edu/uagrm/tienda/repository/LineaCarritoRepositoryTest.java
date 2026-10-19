package bo.edu.uagrm.tienda.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import bo.edu.uagrm.tienda.entity.Categoria;
import bo.edu.uagrm.tienda.entity.LineaCarrito;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;
import bo.edu.uagrm.tienda.entity.Usuario;
import jakarta.persistence.PersistenceException;

// HU-11: las líneas del carrito de cada cliente
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LineaCarritoRepositoryTest {

	private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 17, 9, 0);

	@Autowired
	private TestEntityManager em;

	@Autowired
	private LineaCarritoRepository lineaRepository;

	private Usuario ana;
	private Usuario beto;
	private Producto martillo;
	private Producto cable;

	@BeforeEach
	void preparar() {
		Categoria herramientas = em.persist(Categoria.crear("Herramientas HU-11", null));
		martillo = producto("Martillo", UnidadVenta.UNIDAD, herramientas);
		cable = producto("Cable", UnidadVenta.METRO, herramientas);
		ana = em.persist(Usuario.registrarCliente("Ana", "Paz", "ana.hu11@mail.com", "$2a$10$hash", null, AHORA));
		beto = em.persist(Usuario.registrarCliente("Beto", "Rojas", "beto.hu11@mail.com", "$2a$10$hash", null, AHORA));
		em.flush();
	}

	private Producto producto(String nombre, UnidadVenta unidad, Categoria categoria) {
		Producto producto = em.persist(
				Producto.crear(nombre, null, null, BigDecimal.TEN, unidad, categoria, List.of(), AHORA));
		producto.asignarCodigo("HER-H11-" + producto.getIdProducto());
		return producto;
	}

	// HU-11 RF-2
	@Test
	void unaLineaPorProductoYCliente() {
		em.persist(LineaCarrito.crear(ana, martillo, new BigDecimal("2"), AHORA));

		// Con IDs autoincrementales el INSERT sale en el persist, no en el flush
		assertThatThrownBy(() -> {
			em.persist(LineaCarrito.crear(ana, martillo, new BigDecimal("3"), AHORA.plusMinutes(1)));
			em.flush();
		}).isInstanceOf(PersistenceException.class);
	}

	// HU-11 RF-8
	@Test
	void lasLineasVanEnElOrdenEnQueSeAgregaron() {
		em.persist(LineaCarrito.crear(ana, cable, new BigDecimal("2.5"), AHORA.plusMinutes(5)));
		em.persist(LineaCarrito.crear(ana, martillo, new BigDecimal("2"), AHORA));
		em.flush();
		em.clear();

		List<LineaCarrito> lineas = lineaRepository.findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(
				ana.getIdUsuario());

		assertThat(lineas).extracting(linea -> linea.getProducto().getNombre()).containsExactly("Martillo", "Cable");
		assertThat(lineas.get(1).getCantidad()).isEqualByComparingTo("2.5");
		assertThat(lineas.get(1).getCantidad().scale()).isEqualTo(2);
	}

	// HU-11 RF-13
	@Test
	void cadaClienteVeSoloSusLineas() {
		em.persist(LineaCarrito.crear(ana, martillo, BigDecimal.ONE, AHORA));
		em.persist(LineaCarrito.crear(beto, cable, BigDecimal.ONE, AHORA));
		em.flush();
		em.clear();

		assertThat(lineaRepository.findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(ana.getIdUsuario()))
				.extracting(linea -> linea.getProducto().getNombre()).containsExactly("Martillo");
		assertThat(lineaRepository.countByUsuario_IdUsuario(beto.getIdUsuario())).isEqualTo(1);
		assertThat(lineaRepository.findByUsuario_IdUsuarioAndProducto_IdProducto(ana.getIdUsuario(),
				cable.getIdProducto())).isEmpty();

		lineaRepository.deleteByUsuario_IdUsuario(ana.getIdUsuario());
		em.flush();
		em.clear();

		assertThat(lineaRepository.countByUsuario_IdUsuario(ana.getIdUsuario())).isZero();
		assertThat(lineaRepository.countByUsuario_IdUsuario(beto.getIdUsuario())).isEqualTo(1);
	}
}
