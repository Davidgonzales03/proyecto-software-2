package bo.edu.uagrm.tienda.dto;

import bo.edu.uagrm.tienda.service.pago.EstadoDelCobro;
import jakarta.validation.constraints.NotNull;

// El resultado que se fuerza en el simulador (HU-13 RF-12). PENDIENTE no se fuerza: se rechaza en el controlador
public record SimulacionRequest(@NotNull(message = "es obligatorio") EstadoDelCobro resultado) {
}
