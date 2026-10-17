package bo.edu.uagrm.tienda.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.AssertTrue;

// Carpeta del catálogo de demostración (issue #25): catalogo.csv y sus imágenes. Sin valor no se carga nada. Las
// reglas se validan en CatalogoDemo y solo cuando hay que cargar: por eso no lleva @Validated
@ConfigurationProperties("tienda.demo")
public record CatalogoDemoProperties(Path directorio) {

	public static final String CATALOGO = "catalogo.csv";

	public boolean activa() {
		return directorio != null && !directorio.toString().isBlank();
	}

	@AssertTrue(message = "TIENDA_DEMO_DIRECTORIO tiene que ser una carpeta con " + CATALOGO)
	public boolean isCarpetaConCatalogo() {
		return !activa() || Files.isRegularFile(directorio.resolve(CATALOGO));
	}
}
