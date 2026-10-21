package bo.edu.uagrm.tienda.service;

import java.time.Clock;
import java.time.Duration;

import org.springframework.stereotype.Component;

import bo.edu.uagrm.tienda.exception.ErrorNegocioException;
import bo.edu.uagrm.tienda.exception.SolicitudesExcedidasException;

// HU-20 RF-6: máximo 3 solicitudes de reenvío en 15 minutos por usuario o IP
@Component
public class LimiteSolicitudesVerificacion extends LimiteDeFrecuencia {

	public LimiteSolicitudesVerificacion(Clock clock) {
		super(clock, 3, Duration.ofMinutes(15), Duration.ofMinutes(15));
	}

	@Override
	protected ErrorNegocioException limiteExcedido() {
		return new SolicitudesExcedidasException();
	}
}
