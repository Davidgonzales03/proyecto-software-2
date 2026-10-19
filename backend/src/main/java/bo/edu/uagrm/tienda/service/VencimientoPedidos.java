package bo.edu.uagrm.tienda.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import bo.edu.uagrm.tienda.entity.EstadoPedido;
import bo.edu.uagrm.tienda.repository.PedidoRepository;
import bo.edu.uagrm.tienda.repository.PedidoRepository.PedidoAVencer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Vence los pedidos sin pagar a los 30 minutos, aunque nadie los mire (HU-12 RF-10). Es otro bean que PedidoService:
// así cada pedido vence en su propia transacción y uno que falla no frena a los demás
@Slf4j
@Component
@RequiredArgsConstructor
public class VencimientoPedidos {

	private final PedidoRepository pedidoRepository;
	private final PedidoService pedidoService;
	private final Clock clock;

	// En las pruebas va apagada: cada prueba vence cuando lo pide, con su reloj
	@Value("${tienda.pedidos.vencimiento-programado:true}")
	private boolean programado;

	@Scheduled(fixedDelayString = "PT1M")
	void cadaMinuto() {
		if (programado) {
			vencer();
		}
	}

	public void vencer() {
		for (PedidoAVencer pedido : pedidoRepository.findByEstadoAndVenceLessThanEqual(EstadoPedido.PENDIENTE_PAGO,
				LocalDateTime.now(clock))) {
			try {
				pedidoService.vencer(pedido.idUsuario(), pedido.idPedido());
			} catch (RuntimeException e) {
				log.warn("No se pudo vencer el pedido {}", pedido.idPedido(), e);
			}
		}
	}
}
