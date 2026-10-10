package bo.edu.uagrm.tienda.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Un par de la ficha técnica libre de un producto («Potencia: 750 W»)
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Caracteristica {

	@Column(nullable = false, length = 40)
	private String nombre;

	@Column(nullable = false, length = 100)
	private String valor;

	public Caracteristica(String nombre, String valor) {
		this.nombre = Objects.requireNonNull(nombre).strip();
		this.valor = Objects.requireNonNull(valor).strip();
	}
}
