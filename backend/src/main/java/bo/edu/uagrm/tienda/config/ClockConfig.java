package bo.edu.uagrm.tienda.config;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

	// En microsegundos, lo que guardan las columnas de fecha (datetime(6)). Con más decimales, la base redondea y una
	// hora guardada puede quedar un instante después de la que se comparó: un pedido no vencería justo a los 30
	// minutos (HU-12 RF-10)
	@Bean
	Clock clock() {
		return Clock.tick(Clock.system(ZoneId.of("America/La_Paz")), Duration.ofNanos(1_000));
	}
}
