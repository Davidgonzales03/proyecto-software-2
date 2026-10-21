package bo.edu.uagrm.tienda.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.entity.VerificacionCorreo;
import bo.edu.uagrm.tienda.exception.TokenVerificacionInvalidoException;
import bo.edu.uagrm.tienda.exception.UsuarioNoEncontradoException;
import bo.edu.uagrm.tienda.repository.UsuarioRepository;
import bo.edu.uagrm.tienda.repository.VerificacionCorreoRepository;
import lombok.RequiredArgsConstructor;

// HU-20: Verificación de correo electrónico (RF-1, RF-2, RF-3, RF-5, RF-6)
@Service
@RequiredArgsConstructor
public class VerificacionCorreoService {

	private final UsuarioRepository usuarioRepository;
	private final VerificacionCorreoRepository verificacionRepository;
	private final NotificadorCorreo notificadorCorreo;
	private final LimiteSolicitudesVerificacion limiteSolicitudes;
	private final Clock clock;

	@Transactional
	public void iniciarVerificacion(Usuario usuario) {
		invalidarPendientes(usuario);
		String token = TokenRecuperacion.generar();
		verificacionRepository.save(
				VerificacionCorreo.crear(usuario, TokenRecuperacion.hash(token), LocalDateTime.now(clock)));
		notificadorCorreo.enviarVerificacionCorreo(usuario.getCorreo(), usuario.getNombre(), token);
	}

	@Transactional
	public void solicitarReenvio(Long idUsuario, String ip) {
		Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(UsuarioNoEncontradoException::new);
		if (usuario.isCorreoVerificado()) {
			return;
		}
		limiteSolicitudes.registrarIntento(usuario.getCorreo(), ip);
		iniciarVerificacion(usuario);
	}

	@Transactional
	public void verificar(String token) {
		if (token == null || token.isBlank()) {
			throw new TokenVerificacionInvalidoException();
		}
		LocalDateTime ahora = LocalDateTime.now(clock);
		VerificacionCorreo verificacion = verificacionRepository
				.findByHashToken(TokenRecuperacion.hash(token.trim()))
				.filter(registro -> registro.estaVigente(ahora))
				.orElseThrow(TokenVerificacionInvalidoException::new);

		Usuario usuario = verificacion.getUsuario();
		usuario.marcarCorreoVerificado();
		invalidarPendientes(usuario);
	}

	private void invalidarPendientes(Usuario usuario) {
		verificacionRepository.findByUsuarioAndUsadoFalse(usuario).forEach(VerificacionCorreo::marcarUsada);
	}
}
