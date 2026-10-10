package bo.edu.uagrm.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Un par de la ficha técnica. Los dos lados son obligatorios; el par vacío lo descarta ProductoRequest
public record CaracteristicaRequest(

		@NotBlank(message = "es obligatorio")
		@Size(max = 40, message = "admite hasta 40 caracteres")
		String nombre,

		@NotBlank(message = "es obligatorio")
		@Size(max = 100, message = "admite hasta 100 caracteres")
		String valor) {

	public CaracteristicaRequest {
		nombre = nombre == null ? null : nombre.strip();
		valor = valor == null ? null : valor.strip();
	}

	boolean estaVacia() {
		return (nombre == null || nombre.isEmpty()) && (valor == null || valor.isEmpty());
	}
}
