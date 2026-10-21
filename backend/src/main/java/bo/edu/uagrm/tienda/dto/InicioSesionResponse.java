package bo.edu.uagrm.tienda.dto;

import java.time.Instant;

import bo.edu.uagrm.tienda.entity.Usuario;

public record InicioSesionResponse(String token, String tipo, Instant expiracion, UsuarioResponse usuario) {

	public static InicioSesionResponse from(Usuario usuario, String token, Instant expiracion) {
		return new InicioSesionResponse(token, "Bearer", expiracion, UsuarioResponse.from(usuario));
	}
}
