package bo.edu.uagrm.tienda.service;

import bo.edu.uagrm.tienda.config.TokenEmitido;
import bo.edu.uagrm.tienda.dto.InicioSesionResponse;
import bo.edu.uagrm.tienda.entity.Usuario;

public record SesionIniciada(Usuario usuario, TokenEmitido token) {

	// La respuesta de la API: el DTO no conoce los tipos del servicio
	public InicioSesionResponse respuesta() {
		return InicioSesionResponse.from(usuario, token.token(), token.expiracion());
	}
}
