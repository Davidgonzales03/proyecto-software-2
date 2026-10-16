package bo.edu.uagrm.tienda.dto;

import java.util.List;

// Los carruseles de la portada, en el orden en que se muestran (HU-09 RF-3). Una lista vacía es una sección que no
// se muestra
public record PortadaResponse(List<ProductoTarjetaResponse> destacados, List<ProductoTarjetaResponse> recientes,
		List<SeccionResponse> categorias) {
}
