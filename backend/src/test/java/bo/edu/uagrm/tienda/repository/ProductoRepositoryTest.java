package bo.edu.uagrm.tienda.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import bo.edu.uagrm.tienda.entity.Caracteristica;
import bo.edu.uagrm.tienda.entity.Categoria;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;

// HU-07: productos
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductoRepositoryTest {

	private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 10, 9, 0);

	@Autowired
	private TestEntityManager em;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	private Categoria herramientas;
	private Categoria jardin;

	@BeforeEach
	void preparar() {
		productoRepository.deleteAll();
		categoriaRepository.deleteAll();
		herramientas = em.persist(Categoria.crear("Herramientas", null));
		jardin = em.persist(Categoria.crear("Jardín", null));
		em.flush();
	}

	private Producto producto(String nombre, String marca, Categoria categoria, String codigo, int minutos) {
		Producto producto = Producto.crear(nombre, marca, null, new BigDecimal("10.00"), UnidadVenta.UNIDAD, categoria,
				List.of(), AHORA.plusMinutes(minutos));
		em.persist(producto);
		producto.asignarCodigo(codigo);
		return producto;
	}

	// HU-07 RF-2, RF-6, RF-8
	@Test
	void guardaYLeeTodasLasColumnasConFichaEImagenesEnOrden() {
		Producto taladro = Producto.crear("Taladro percutor 750 W", "Bosch", "Para concreto y madera",
				new BigDecimal("420.00"), UnidadVenta.UNIDAD, herramientas,
				List.of(new Caracteristica("Potencia", "750 W"), new Caracteristica("Mandril", "13 mm")), AHORA);
		taladro.agregarImagen("3f2b1c4d-0000-4000-8000-000000000001.jpg");
		taladro.agregarImagen("3f2b1c4d-0000-4000-8000-000000000002.png");
		Long id = em.persistAndGetId(taladro, Long.class);
		taladro.asignarCodigo("HER-0001");
		em.flush();
		em.clear();

		Producto leido = productoRepository.findById(id).orElseThrow();

		assertThat(leido.getCodigo()).isEqualTo("HER-0001");
		assertThat(leido.getNombre()).isEqualTo("Taladro percutor 750 W");
		assertThat(leido.getMarca()).isEqualTo("Bosch");
		assertThat(leido.getPrecio()).isEqualByComparingTo("420.00");
		assertThat(leido.getUnidad()).isEqualTo(UnidadVenta.UNIDAD);
		assertThat(leido.getCategoria().getNombre()).isEqualTo("Herramientas");
		assertThat(leido.isActivo()).isTrue();
		assertThat(leido.getFicha()).extracting(Caracteristica::getNombre).containsExactly("Potencia", "Mandril");
		assertThat(leido.getImagenes()).containsExactly("3f2b1c4d-0000-4000-8000-000000000001.jpg",
				"3f2b1c4d-0000-4000-8000-000000000002.png");
	}

	// HU-07 RF-10
	@Test
	void buscarCombinaCategoriaEstadoYTextoEnNombreCodigoYMarca() {
		producto("Taladro", "Bosch", herramientas, "HER-0001", 0);
		producto("Bordeadora", "Bosch", jardin, "JAR-0002", 1);
		Producto inactivo = producto("Tijera de podar", "Bosch", jardin, "JAR-0003", 2);
		inactivo.desactivar();
		producto("Manguera", "Tramontina", jardin, "JAR-0004", 3);
		em.flush();
		em.clear();
		PageRequest pagina = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fechaAlta", "idProducto"));

		assertThat(productoRepository.buscar(jardin.getIdCategoria(), true, "%bosch%", pagina))
				.extracting(Producto::getNombre).containsExactly("Bordeadora");
		assertThat(productoRepository.buscar(null, null, "%jar-000%", pagina))
				.extracting(Producto::getNombre).containsExactly("Manguera", "Tijera de podar", "Bordeadora");
		assertThat(productoRepository.buscar(null, null, null, pagina)).hasSize(4);
	}

	// HU-07 RF-12, RF-13
	@Test
	void contarPorCategoriaCuentaActivosEInactivos() {
		producto("Pala", null, jardin, "JAR-0001", 0);
		producto("Rastrillo", null, jardin, "JAR-0002", 1).desactivar();
		em.flush();

		Map<Long, Long> conteo = productoRepository.contarPorCategoria().stream()
				.collect(Collectors.toMap(ProductoRepository.ConteoPorCategoria::idCategoria,
						ProductoRepository.ConteoPorCategoria::productos));

		assertThat(conteo).containsEntry(jardin.getIdCategoria(), 2L).doesNotContainKey(herramientas.getIdCategoria());
		assertThat(productoRepository.existsByCategoria_IdCategoria(jardin.getIdCategoria())).isTrue();
		assertThat(productoRepository.existsByCategoria_IdCategoria(herramientas.getIdCategoria())).isFalse();
	}

	// HU-08 RF-2
	@Test
	void productoNuevoEmpiezaConStockCeroYSinMinimo() {
		Long id = producto("Pala", null, jardin, "JAR-0001", 0).getIdProducto();
		em.flush();
		em.clear();

		Producto leido = productoRepository.findById(id).orElseThrow();

		assertThat(leido.getStock()).isEqualByComparingTo("0");
		assertThat(leido.getStockMinimo()).isNull();
		assertThat(leido.isStockBajo()).isFalse();
	}

	// HU-08 RF-9, RF-10: por nombre, con texto en nombre o código, categoría, estado y solo stock bajo
	@Test
	void inventarioFiltraPorTextoCategoriaEstadoYStockBajo() {
		Producto taladro = producto("Taladro", "Bosch", herramientas, "HER-0001", 0);
		taladro.aplicarMovimiento(new BigDecimal("3"));
		taladro.cambiarMinimo(new BigDecimal("5"));
		Producto pala = producto("Pala", null, jardin, "JAR-0002", 1);
		pala.aplicarMovimiento(new BigDecimal("5"));
		pala.cambiarMinimo(new BigDecimal("5"));
		Producto manguera = producto("Manguera", null, jardin, "JAR-0003", 2);
		manguera.aplicarMovimiento(new BigDecimal("6"));
		manguera.cambiarMinimo(new BigDecimal("5"));
		producto("Azada", null, jardin, "JAR-0004", 3).desactivar();
		em.flush();
		em.clear();
		PageRequest pagina = PageRequest.of(0, 20, Sort.by("nombre", "idProducto"));

		assertThat(productoRepository.inventario(null, null, null, false, pagina)).extracting(Producto::getNombre)
				.containsExactly("Azada", "Manguera", "Pala", "Taladro");
		assertThat(productoRepository.inventario(null, null, null, true, pagina)).extracting(Producto::getNombre)
				.containsExactly("Pala", "Taladro");
		assertThat(productoRepository.inventario(null, jardin.getIdCategoria(), true, false, pagina))
				.extracting(Producto::getNombre).containsExactly("Manguera", "Pala");
		assertThat(productoRepository.inventario("%jar-0004%", null, false, false, pagina))
				.extracting(Producto::getNombre).containsExactly("Azada");
		assertThat(productoRepository.inventario("%tala%", null, null, false, pagina))
				.extracting(Producto::getNombre).containsExactly("Taladro");
	}

	// HU-07 RF-4: el código se exige único en la base, aunque lo asigne el servicio
	@Test
	void codigoRepetidoSeRechaza() {
		producto("Pala", null, jardin, "JAR-0001", 0);
		em.flush();
		Producto otro = Producto.crear("Rastrillo", null, null, BigDecimal.ONE, UnidadVenta.UNIDAD, jardin, List.of(),
				AHORA);
		otro.asignarCodigo("JAR-0001");

		assertThatThrownBy(() -> productoRepository.saveAndFlush(otro))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
