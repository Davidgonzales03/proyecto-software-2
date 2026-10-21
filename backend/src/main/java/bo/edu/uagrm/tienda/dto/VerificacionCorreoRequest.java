package bo.edu.uagrm.tienda.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificacionCorreoRequest(@NotBlank(message = "El token es obligatorio") String token) {
}
