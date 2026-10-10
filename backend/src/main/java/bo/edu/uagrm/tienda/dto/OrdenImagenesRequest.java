package bo.edu.uagrm.tienda.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

// Todas las imágenes del producto en el orden nuevo; la primera pasa a ser la principal
public record OrdenImagenesRequest(@NotNull(message = "es obligatorio") List<String> imagenes) {
}
