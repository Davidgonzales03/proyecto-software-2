package bo.edu.uagrm.tienda.service.pago;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import bo.edu.uagrm.tienda.config.ClockConfig;
import bo.edu.uagrm.tienda.config.LibelulaConfig;
import bo.edu.uagrm.tienda.config.LibelulaProperties;
import bo.edu.uagrm.tienda.controller.SimulacionDePagoController;
import bo.edu.uagrm.tienda.service.PagoService;

// HU-13 RF-13 (decisiones.md → Pago: interfaz propia con dos implementaciones), elegidas con tienda.pago.proveedor
class SeleccionDelProveedorDePagoTest {

	private final ApplicationContextRunner contexto = new ApplicationContextRunner()
			.withUserConfiguration(ClockConfig.class, SimuladorDePago.class, LibelulaConfig.class)
			.withPropertyValues("tienda.pago.libelula.url=https://api.libelula.bo",
					"tienda.pago.libelula.url-del-sitio=https://tienda.test");

	private ApplicationContextRunner conLibelula() {
		return contexto.withPropertyValues("tienda.pago.proveedor=libelula",
				"tienda.pago.libelula.appkey=clave-de-prueba", "tienda.pago.libelula.email=pagos@tienda.test");
	}

	@Test
	void sinProveedorSeUsaElSimulador() {
		contexto.run(ctx -> assertThat(ctx).getBean(IPago.class).isInstanceOf(SimuladorDePago.class));
	}

	@Test
	void conLibelulaSeUsaElAdaptador() {
		conLibelula().run(ctx -> {
			assertThat(ctx).hasSingleBean(IPago.class).doesNotHaveBean(SimuladorDePago.class);
			assertThat(ctx).getBean(IPago.class).isInstanceOf(AdaptadorLibelula.class);
		});
	}

	@Test
	void conLibelulaSinAppkeyNoArranca() {
		conLibelula().withPropertyValues("tienda.pago.libelula.appkey=").run(ctx -> assertThat(ctx).hasFailed()
				.getFailure().hasStackTraceContaining("tienda.pago.libelula.appkey"));
	}

	@Test
	void conLibelulaSinCorreoNoArranca() {
		conLibelula().withPropertyValues("tienda.pago.libelula.email=").run(ctx -> assertThat(ctx).hasFailed()
				.getFailure().hasStackTraceContaining("tienda.pago.libelula.email"));
	}

	// La appkey no aparece si se imprime la configuración
	@Test
	void laConfiguracionNoMuestraLaAppkey() {
		conLibelula().run(ctx -> assertThat(ctx.getBean(LibelulaProperties.class)
				.toString()).doesNotContain("clave-de-prueba"));
	}

	// HU-13 RF-12: los botones del simulador tienen su ruta solo con el simulador
	@Test
	void laSimulacionSoloExisteConElSimulador() {
		ApplicationContextRunner conControlador = contexto.withUserConfiguration(SimulacionDePagoController.class)
				.withBean(PagoService.class, () -> mock(PagoService.class));

		conControlador.run(ctx -> assertThat(ctx).hasSingleBean(SimulacionDePagoController.class));
		conControlador.withPropertyValues("tienda.pago.proveedor=libelula", "tienda.pago.libelula.appkey=clave-de-prueba",
				"tienda.pago.libelula.email=pagos@tienda.test")
				.run(ctx -> assertThat(ctx).hasNotFailed().doesNotHaveBean(SimulacionDePagoController.class));
	}
}
