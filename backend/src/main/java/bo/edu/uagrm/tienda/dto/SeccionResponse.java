package bo.edu.uagrm.tienda.dto;

import java.util.List;

// El carrusel de una categoría en la portada (HU-09 RF-3)
public record SeccionResponse(Long idCategoria, String nombre, List<ProductoTarjetaResponse> productos) {
}
