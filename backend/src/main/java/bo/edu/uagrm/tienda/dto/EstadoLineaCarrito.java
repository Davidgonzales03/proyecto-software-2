package bo.edu.uagrm.tienda.dto;

// Si una línea del carrito se puede comprar hoy (HU-11 RF-9). Solo DISPONIBLE suma al total
public enum EstadoLineaCarrito {
	DISPONIBLE, AGOTADO, NO_DISPONIBLE, CANTIDAD_INVALIDA
}
