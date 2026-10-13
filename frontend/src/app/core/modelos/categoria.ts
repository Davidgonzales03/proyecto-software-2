// Espejo de CategoriaResponse: lo público, y las categorías del filtro del inventario
export interface Categoria {
	idCategoria: number;
	nombre: string;
	descripcion: string | null;
	tieneImagen: boolean;
}

// Espejo de CategoriaGestionResponse (gestión) y CategoriaRequest del backend
export interface CategoriaGestion {
	idCategoria: number;
	nombre: string;
	descripcion: string | null;
	tieneImagen: boolean;
	visible: boolean;
	// Cuántos productos tiene, activos o no (HU-07)
	productos: number;
}

export interface DatosDeCategoria {
	nombre: string;
	descripcion: string;
}
