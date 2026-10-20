package bo.edu.uagrm.tienda.dto;

import bo.edu.uagrm.tienda.entity.Cobro;

// El QR que el Cliente paga y, si el sistema de pago la da, su página de pago (HU-13 RF-1). simulador: la página
// muestra «Simular pago» y «Simular error» (RF-12)
public record CobroResponse(String codigoQr, String urlPasarela, boolean simulador) {

	public static CobroResponse from(Cobro cobro, boolean simulador) {
		return new CobroResponse(cobro.getCodigoQr(), cobro.getUrlPasarela(), simulador);
	}
}
