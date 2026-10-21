package bo.edu.uagrm.tienda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.entity.VerificacionCorreo;
import bo.edu.uagrm.tienda.exception.SolicitudesExcedidasException;
import bo.edu.uagrm.tienda.exception.TokenVerificacionInvalidoException;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import bo.edu.uagrm.tienda.repository.VerificacionCorreoRepository;

@ExtendWith(MockitoExtension.class)
class VerificacionCorreoServiceTest {

	private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-21T10:00:00Z"), ZoneId.of("America/La_Paz"));
	private static final String IP = "10.0.0.1";

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private VerificacionCorreoRepository verificacionRepository;

	@Mock
	private NotificadorCorreo notificadorCorreo;

	private LimiteSolicitudesVerificacion limiteSolicitudes;
	private VerificacionCorreoService servicio;

	@BeforeEach
	void preparar() {
		limiteSolicitudes = new LimiteSolicitudesVerificacion(RELOJ);
		servicio = new VerificacionCorreoService(usuarioRepository, verificacionRepository, notificadorCorreo,
				limiteSolicitudes, RELOJ);
	}

	private static Usuario ana() {
		return Usuario.registrarCliente("Ana", "Rojas", "ana@mail.com", "$2a$10$hash", null,
				LocalDateTime.of(2026, 10, 21, 9, 0));
	}

	// HU-20 RF-1
	@Test
	void iniciarVerificacionGuardaElHashYEnviaElCorreo() {
		Usuario ana = ana();
		given(verificacionRepository.findByUsuarioAndUsadoFalse(ana)).willReturn(List.of());

		servicio.iniciarVerificacion(ana);

		ArgumentCaptor<VerificacionCorreo> captor = ArgumentCaptor.forClass(VerificacionCorreo.class);
		then(verificacionRepository).should().save(captor.capture());
		VerificacionCorreo guardada = captor.getValue();
		assertThat(guardada.getUsuario()).isSameAs(ana);
		assertThat(guardada.getHashToken()).isNotBlank();
		assertThat(guardada.isUsado()).isFalse();

		then(notificadorCorreo).should().enviarVerificacionCorreo(eq("ana@mail.com"), eq("Ana"), any(String.class));
	}

	// HU-20 RF-2
	@Test
	void verificarMarcaElCorreoComoVerificadoEInvalidaElToken() {
		Usuario ana = ana();
		LocalDateTime ahora = LocalDateTime.now(RELOJ);
		String tokenPlano = TokenRecuperacion.generar();
		String hash = TokenRecuperacion.hash(tokenPlano);
		VerificacionCorreo verificacion = VerificacionCorreo.crear(ana, hash, ahora);

		given(verificacionRepository.findByHashToken(hash)).willReturn(Optional.of(verificacion));
		given(verificacionRepository.findByUsuarioAndUsadoFalse(ana)).willReturn(List.of(verificacion));

		servicio.verificar(tokenPlano);

		assertThat(ana.isCorreoVerificado()).isTrue();
		assertThat(verificacion.isUsado()).isTrue();
	}

	// HU-20 RF-3
	@Test
	void tokenInvalidoOVencidoLanzaExcepcion() {
		assertThatThrownBy(() -> servicio.verificar(null))
				.isInstanceOf(TokenVerificacionInvalidoException.class);
		assertThatThrownBy(() -> servicio.verificar("   "))
				.isInstanceOf(TokenVerificacionInvalidoException.class);

		String token = TokenRecuperacion.generar();
		given(verificacionRepository.findByHashToken(TokenRecuperacion.hash(token))).willReturn(Optional.empty());
		assertThatThrownBy(() -> servicio.verificar(token))
				.isInstanceOf(TokenVerificacionInvalidoException.class);
	}

	// HU-20 RF-5
	@Test
	void solicitarReenvioParaUsuarioNoVerificadoEnviaNuevoToken() {
		Usuario ana = ana();
		given(usuarioRepository.findById(1L)).willReturn(Optional.of(ana));
		given(verificacionRepository.findByUsuarioAndUsadoFalse(ana)).willReturn(List.of());

		servicio.solicitarReenvio(1L, IP);

		then(verificacionRepository).should().save(any(VerificacionCorreo.class));
		then(notificadorCorreo).should().enviarVerificacionCorreo(eq("ana@mail.com"), eq("Ana"), any(String.class));
	}

	// HU-20 RF-5: si ya está verificado, no hace nada
	@Test
	void solicitarReenvioParaUsuarioYaVerificadoNoHaceNada() {
		Usuario ana = ana();
		ana.marcarCorreoVerificado();
		given(usuarioRepository.findById(1L)).willReturn(Optional.of(ana));

		servicio.solicitarReenvio(1L, IP);

		then(verificacionRepository).should(never()).save(any());
		then(notificadorCorreo).should(never()).enviarVerificacionCorreo(any(), any(), any());
	}

	// HU-20 RF-6
	@Test
	void masDeTresSolicitudesEnQuinceMinutosBloqueanElReenvio() {
		Usuario ana = ana();
		given(usuarioRepository.findById(1L)).willReturn(Optional.of(ana));
		given(verificacionRepository.findByUsuarioAndUsadoFalse(ana)).willReturn(List.of());

		servicio.solicitarReenvio(1L, IP);
		servicio.solicitarReenvio(1L, IP);
		servicio.solicitarReenvio(1L, IP);

		assertThatThrownBy(() -> servicio.solicitarReenvio(1L, IP))
				.isInstanceOf(SolicitudesExcedidasException.class);
	}
}
