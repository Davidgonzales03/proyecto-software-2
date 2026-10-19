package bo.edu.uagrm.tienda.dto;

import bo.edu.uagrm.tienda.config.RetiroProperties;

// Dónde y cuándo retirar (HU-12 RF-17); cada uno es null si no está configurado
public record RetiroResponse(String direccion, String horario) {

	public static RetiroResponse de(RetiroProperties propiedades) {
		return new RetiroResponse(textoONulo(propiedades.direccion()), textoONulo(propiedades.horario()));
	}

	private static String textoONulo(String texto) {
		return texto == null || texto.isBlank() ? null : texto.strip();
	}
}
