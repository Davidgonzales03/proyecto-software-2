package bo.edu.uagrm.tienda.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

// HU-20 RF-1, RF-2, RF-3: vigencia de 24 horas y un solo uso
class VerificacionCorreoTest {

	private static Usuario ana() {
		return Usuario.registrarCliente("Ana", "Rojas", "ana@mail.com", "$2a$10$hash", null,
				LocalDateTime.of(2026, 10, 21, 9, 0));
	}

	// HU-20 RF-3
	@Test
	void naceVigenteYVenceALas24Horas() {
		LocalDateTime creacion = LocalDateTime.of(2026, 10, 21, 9, 0);
		VerificacionCorreo verificacion = VerificacionCorreo.crear(ana(), "hash", creacion);

		assertThat(verificacion.estaVigente(creacion)).isTrue();
		assertThat(verificacion.estaVigente(creacion.plusHours(23).plusMinutes(59))).isTrue();
		assertThat(verificacion.estaVigente(creacion.plusHours(24))).isFalse();
	}

	// HU-20 RF-3
	@Test
	void marcarUsadaDejaDeEstarVigente() {
		LocalDateTime creacion = LocalDateTime.of(2026, 10, 21, 9, 0);
		VerificacionCorreo verificacion = VerificacionCorreo.crear(ana(), "hash", creacion);

		verificacion.marcarUsada();

		assertThat(verificacion.estaVigente(creacion)).isFalse();
	}
}
