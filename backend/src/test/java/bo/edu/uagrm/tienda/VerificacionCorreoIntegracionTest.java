package bo.edu.uagrm.tienda;

import static bo.edu.uagrm.tienda.IpsDePrueba.ipNueva;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import bo.edu.uagrm.tienda.entity.Categoria;
import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;
import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.entity.VerificacionCorreo;
import bo.edu.uagrm.tienda.repository.CategoriaRepository;
import bo.edu.uagrm.tienda.repository.LineaCarritoRepository;
import bo.edu.uagrm.tienda.repository.MovimientoStockRepository;
import bo.edu.uagrm.tienda.repository.PedidoRepository;
import bo.edu.uagrm.tienda.repository.ProductoRepository;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import bo.edu.uagrm.tienda.repository.VerificacionCorreoRepository;
import bo.edu.uagrm.tienda.service.TokenRecuperacion;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerificacionCorreoIntegracionTest {

	private static final String REGISTRO = """
			{"nombre":"Ana","apellido":"Rojas","correo":"ana.hu20@mail.com","contrasena":"secreta12"}
			""";

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private VerificacionCorreoRepository verificacionRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ProductoRepository productoRepository;

	@Autowired
	private LineaCarritoRepository lineaCarritoRepository;

	@Autowired
	private PedidoRepository pedidoRepository;

	@Autowired
	private MovimientoStockRepository movimientoRepository;

	@BeforeEach
	@AfterEach
	void limpiar() {
		lineaCarritoRepository.deleteAll();
		movimientoRepository.deleteAll();
		pedidoRepository.deleteAll();
		productoRepository.deleteAll();
		categoriaRepository.deleteAll();
		verificacionRepository.deleteAll();
		usuarioRepository.findByCorreo("ana.hu20@mail.com").ifPresent(usuarioRepository::delete);
	}

	private MvcTestResult registrar() {
		return mvc.post().uri("/api/auth/registro").with(ipNueva()).contentType(MediaType.APPLICATION_JSON)
				.content(REGISTRO).exchange();
	}

	// HU-20 RF-1: cliente se registra con correo no verificado y se crea el token de verificación
	@Test
	void registroCreaUsuarioConCorreoNoVerificadoYTokenDeVerificacion() {
		MvcTestResult respuesta = registrar();
		assertThat(respuesta).hasStatus(HttpStatus.CREATED);
		assertThat(respuesta).bodyJson().extractingPath("$.usuario.correoVerificado").isEqualTo(false);

		Usuario usuario = usuarioRepository.findByCorreo("ana.hu20@mail.com").orElseThrow();
		assertThat(usuario.isCorreoVerificado()).isFalse();

		List<VerificacionCorreo> tokens = verificacionRepository.findByUsuarioAndUsadoFalse(usuario);
		assertThat(tokens).hasSize(1);
		assertThat(tokens.get(0).getHashToken()).isNotBlank();
	}

	// HU-20 RF-2: verificación con token válido marca el correo como verificado
	@Test
	void verificarConTokenValidoMarcaElCorreoComoVerificado() {
		registrar();
		Usuario usuario = usuarioRepository.findByCorreo("ana.hu20@mail.com").orElseThrow();
		String tokenPlano = TokenRecuperacion.generar();
		verificacionRepository.save(VerificacionCorreo.crear(usuario, TokenRecuperacion.hash(tokenPlano), LocalDateTime.now()));

		MvcTestResult respuesta = mvc.post().uri("/api/auth/verificacion-correo")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"%s\"}".formatted(tokenPlano))
				.exchange();

		assertThat(respuesta).hasStatus(HttpStatus.OK);
		assertThat(respuesta).bodyJson().extractingPath("$.mensaje").asString().contains("verificado correctamente");

		Usuario verificado = usuarioRepository.findByCorreo("ana.hu20@mail.com").orElseThrow();
		assertThat(verificado.isCorreoVerificado()).isTrue();
	}

	// HU-20 RF-3: token inválido o inexistente responde 400
	@Test
	void verificarConTokenInvalidoDevuelve400() {
		MvcTestResult respuesta = mvc.post().uri("/api/auth/verificacion-correo")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"token-inexistente\"}")
				.exchange();

		assertThat(respuesta).hasStatus(HttpStatus.BAD_REQUEST);
		assertThat(respuesta).bodyJson().extractingPath("$.codigo").isEqualTo("TOKEN_VERIFICACION_INVALIDO");
	}

	// HU-20 RF-4: cliente con correo no verificado no puede confirmar pedido
	@Test
	void clienteSinCorreoVerificadoNoPuedeConfirmarPedido() throws Exception {
		MvcTestResult registro = registrar();
		String tokenJwt = JsonPath.read(registro.getResponse().getContentAsString(), "$.token");

		Categoria cat = categoriaRepository.save(Categoria.crear("Herramientas", null));
		Producto prod = Producto.crear("Martillo", null, null, new BigDecimal("85.50"), UnidadVenta.UNIDAD, cat, List.of(), LocalDateTime.now());
		org.springframework.test.util.ReflectionTestUtils.setField(prod, "stock", new BigDecimal("10"));
		prod = productoRepository.save(prod);

		// Agrega al carrito
		mvc.post().uri("/api/carrito/lineas").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"idProducto\":%d,\"cantidad\":1}".formatted(prod.getIdProducto()))
				.exchange();

		// Intenta confirmar pedido
		MvcTestResult pedido = mvc.post().uri("/api/pedidos").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJwt)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombreRetira\":\"Ana\",\"telefonoRetira\":\"70000000\",\"nota\":null,\"totalEsperado\":85.50}")
				.exchange();

		assertThat(pedido).hasStatus(HttpStatus.CONFLICT);
		assertThat(pedido).bodyJson().extractingPath("$.codigo").isEqualTo("CORREO_NO_VERIFICADO");
	}

	// HU-20 RF-5: reenvío de correo genera un nuevo token
	@Test
	void reenvioDeVerificacionGeneraNuevoToken() throws Exception {
		MvcTestResult registro = registrar();
		String tokenJwt = JsonPath.read(registro.getResponse().getContentAsString(), "$.token");
		Usuario usuario = usuarioRepository.findByCorreo("ana.hu20@mail.com").orElseThrow();

		MvcTestResult reenvio = mvc.post().uri("/api/auth/verificacion-correo/reenvio")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJwt)
				.with(ipNueva())
				.exchange();

		assertThat(reenvio).hasStatus(HttpStatus.ACCEPTED);
		List<VerificacionCorreo> pendientes = verificacionRepository.findByUsuarioAndUsadoFalse(usuario);
		assertThat(pendientes).hasSize(1);
	}

	// HU-20 RF-6: límite de reenvíos
	@Test
	void limiteDeReenviosResponde429AlExcederTres() throws Exception {
		MvcTestResult registro = registrar();
		String tokenJwt = JsonPath.read(registro.getResponse().getContentAsString(), "$.token");

		for (int i = 0; i < 3; i++) {
			assertThat(mvc.post().uri("/api/auth/verificacion-correo/reenvio")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJwt)
					.exchange()).hasStatus(HttpStatus.ACCEPTED);
		}

		MvcTestResult cuarto = mvc.post().uri("/api/auth/verificacion-correo/reenvio")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenJwt)
				.exchange();

		assertThat(cuarto).hasStatus(HttpStatus.TOO_MANY_REQUESTS);
		assertThat(cuarto).bodyJson().extractingPath("$.codigo").isEqualTo("SOLICITUDES_EXCEDIDAS");
	}
}
