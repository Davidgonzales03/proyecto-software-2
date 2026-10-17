package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.entity.UnidadVenta;

// Una línea del carrito con el precio de hoy (HU-11 RF-8). Sin stock (RF-14); imagen es null si el producto no tiene
// fotos y subtotal, si la línea no se puede comprar (RF-9)
public record LineaCarritoResponse(Long idProducto, String nombre, String imagen, BigDecimal precio, UnidadVenta unidad,
		BigDecimal cantidad, BigDecimal subtotal, EstadoLineaCarrito estado) {
}
