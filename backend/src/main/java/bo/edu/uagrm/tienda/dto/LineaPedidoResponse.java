package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.entity.LineaPedido;
import bo.edu.uagrm.tienda.entity.UnidadVenta;

// Una línea del pedido como se confirmó (HU-12 RF-2): no cambia si el producto cambia después
public record LineaPedidoResponse(Long idProducto, String nombre, UnidadVenta unidad, BigDecimal precio,
		BigDecimal cantidad, BigDecimal subtotal) {

	public static LineaPedidoResponse from(LineaPedido linea) {
		return new LineaPedidoResponse(linea.getIdProducto(), linea.getNombre(), linea.getUnidad(), linea.getPrecio(),
				linea.getCantidad(), linea.getSubtotal());
	}
}
