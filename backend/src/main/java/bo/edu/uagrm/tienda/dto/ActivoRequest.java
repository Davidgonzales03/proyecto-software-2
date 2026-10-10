package bo.edu.uagrm.tienda.dto;

import jakarta.validation.constraints.NotNull;

public record ActivoRequest(@NotNull(message = "es obligatorio") Boolean activo) {
}
