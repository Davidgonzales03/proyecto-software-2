package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.dto.validacion.CantidadDeCarrito;

// Cambiar la cantidad de una línea del carrito (HU-11 RF-10), con las mismas reglas que al agregar
public record CantidadRequest(

		@CantidadDeCarrito
		BigDecimal cantidad) {
}
