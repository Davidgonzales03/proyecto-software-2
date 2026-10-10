package bo.edu.uagrm.tienda.dto;

import bo.edu.uagrm.tienda.entity.Caracteristica;

public record CaracteristicaResponse(String nombre, String valor) {

	public static CaracteristicaResponse from(Caracteristica caracteristica) {
		return new CaracteristicaResponse(caracteristica.getNombre(), caracteristica.getValor());
	}
}
