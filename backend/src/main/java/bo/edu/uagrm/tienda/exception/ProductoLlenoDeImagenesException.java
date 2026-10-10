package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class ProductoLlenoDeImagenesException extends ErrorNegocioException {

	public ProductoLlenoDeImagenesException() {
		super("PRODUCTO_LLENO_DE_IMAGENES", HttpStatus.CONFLICT, "El producto ya tiene 5 imágenes: quite una antes de subir otra.");
	}
}
