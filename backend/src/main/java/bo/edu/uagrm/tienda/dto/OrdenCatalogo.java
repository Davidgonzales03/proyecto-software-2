package bo.edu.uagrm.tienda.dto;

import org.springframework.data.domain.Sort;

// Los órdenes del catálogo (HU-09 RF-10). Los agotados van al final en todos: lo pone la consulta, antes de este
// orden. El número del producto desempata, para que una página no repita ni pierda productos
public enum OrdenCatalogo {

	RECIENTES(Sort.by(Sort.Direction.DESC, "fechaAlta", "idProducto")),
	PRECIO_ASC(Sort.by(Sort.Order.asc("precio"), Sort.Order.desc("idProducto"))),
	PRECIO_DESC(Sort.by(Sort.Order.desc("precio"), Sort.Order.desc("idProducto"))),
	NOMBRE(Sort.by(Sort.Order.asc("nombre"), Sort.Order.asc("idProducto")));

	private final Sort orden;

	OrdenCatalogo(Sort orden) {
		this.orden = orden;
	}

	public Sort orden() {
		return orden;
	}
}
