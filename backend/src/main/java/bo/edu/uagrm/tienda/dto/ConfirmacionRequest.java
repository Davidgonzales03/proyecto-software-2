package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Confirmar el carrito (HU-12 RF-6, RF-15). Los largos se cuentan sin los espacios de alrededor. El total esperado es
// el que vio el cliente: solo se compara, el pedido se arma con los precios del servidor. El cliente sale del token
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmacionRequest(

		@NotBlank(message = "es obligatorio")
		@Size(max = 160, message = "admite hasta 160 caracteres")
		String nombreRetira,

		@NotBlank(message = "es obligatorio")
		@Size(max = 20, message = "admite hasta 20 caracteres")
		String telefonoRetira,

		@Size(max = 200, message = "admite hasta 200 caracteres")
		String nota,

		@NotNull(message = "es obligatorio")
		BigDecimal totalEsperado) {

	public ConfirmacionRequest {
		nombreRetira = nombreRetira == null ? null : nombreRetira.strip();
		telefonoRetira = telefonoRetira == null ? null : telefonoRetira.strip();
		nota = nota == null ? null : nota.strip();
	}
}
