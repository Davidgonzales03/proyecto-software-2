package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.entity.Producto;
import bo.edu.uagrm.tienda.entity.UnidadVenta;

// Una fila del inventario y la cabecera del stock de un producto (HU-08)
public record InventarioResponse(Long idProducto, String codigo, String nombre, String categoria, UnidadVenta unidad,
		boolean activo, BigDecimal stock, BigDecimal stockMinimo, boolean stockBajo) {

	// Lee la categoría: dentro de la transacción
	public static InventarioResponse from(Producto producto) {
		return new InventarioResponse(producto.getIdProducto(), producto.getCodigo(), producto.getNombre(),
				producto.getCategoria().getNombre(), producto.getUnidad(), producto.isActivo(), producto.getStock(),
				producto.getStockMinimo(), producto.isStockBajo());
	}
}
