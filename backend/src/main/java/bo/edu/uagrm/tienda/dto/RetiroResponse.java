package bo.edu.uagrm.tienda.dto;

// Dónde y cuándo retirar (HU-12 RF-17); cada uno es null si no está configurado
public record RetiroResponse(String direccion, String horario) {

	public static RetiroResponse de(String direccion, String horario) {
		return new RetiroResponse(textoONulo(direccion), textoONulo(horario));
	}

	private static String textoONulo(String texto) {
		return texto == null || texto.isBlank() ? null : texto.strip();
	}
}
