package bo.edu.uagrm.tienda.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// decisiones.md → Contraseñas con BCrypt: costo 12 en producción. Una contraseña guardada con el costo anterior (10)
// sigue sirviendo: el costo va dentro del hash
class CostoBcryptTest {

	private final PasswordEncoder codificador = new SecurityConfig().passwordEncoder(12);

	@Test
	void lasContrasenasNuevasVanConCosto12() {
		assertThat(codificador.encode("secreta12")).startsWith("$2a$12$");
	}

	@Test
	void unaContrasenaGuardadaConCosto10SigueSirviendo() {
		String anterior = new BCryptPasswordEncoder(10).encode("secreta12");

		assertThat(codificador.matches("secreta12", anterior)).isTrue();
		assertThat(codificador.matches("otra-clave", anterior)).isFalse();
	}
}
