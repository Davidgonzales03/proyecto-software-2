package bo.edu.uagrm.tienda.service;

import java.util.Locale;
import java.util.function.UnaryOperator;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

// Lo que comparten las búsquedas por texto y los listados paginados de la gestión (productos, inventario, usuarios y
// catálogo): el texto como patrón LIKE, con sus comodines escapados, y la página pedida dentro de los límites
final class Busquedas {

	static final int TAMANO_POR_DEFECTO = 20;
	static final int TAMANO_MAXIMO = 100;

	private Busquedas() {
	}

	// En minúsculas y sin los espacios de alrededor; null si no hay texto (no filtra)
	static String patron(String texto) {
		return patron(texto, valor -> valor.strip().toLowerCase(Locale.ROOT));
	}

	// Con la normalización de quien busca (el catálogo, por ejemplo, también quita las tildes). Los repositorios usan
	// ESCAPE '!': '!' primero, para no volver a escapar los que agregan las otras dos sustituciones
	static String patron(String texto, UnaryOperator<String> normalizar) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		String literal = normalizar.apply(texto).replace("!", "!!").replace("%", "!%").replace("_", "!_");
		return "%" + literal + "%";
	}

	// Igual que el Pageable de Spring Data: una página negativa es la primera y un tamaño inválido, el por defecto
	static PageRequest pagina(int pagina, int tamano, Sort orden) {
		return PageRequest.of(Math.max(pagina, 0), tamano < 1 ? TAMANO_POR_DEFECTO : Math.min(tamano, TAMANO_MAXIMO),
				orden);
	}
}
