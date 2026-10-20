package bo.edu.uagrm.tienda.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import bo.edu.uagrm.tienda.dto.PedidoResponse;
import bo.edu.uagrm.tienda.entity.Cobro;
import bo.edu.uagrm.tienda.entity.Pedido;
import bo.edu.uagrm.tienda.exception.CobroNoDisponibleException;
import bo.edu.uagrm.tienda.exception.CobroNoEncontradoException;
import bo.edu.uagrm.tienda.exception.PedidoNoEncontradoException;
import bo.edu.uagrm.tienda.exception.PedidoNoPendienteException;
import bo.edu.uagrm.tienda.repository.CobroRepository;
import bo.edu.uagrm.tienda.repository.PedidoRepository;
import bo.edu.uagrm.tienda.service.pago.CobroGenerado;
import bo.edu.uagrm.tienda.service.pago.IPago;
import bo.edu.uagrm.tienda.service.pago.SistemaDePagoNoDisponibleException;
import lombok.extern.slf4j.Slf4j;

// El pago del pedido para el Cliente (HU-13; decisiones.md → Pagos): el cobro y su consulta. Aplicar lo que dice el
// sistema de pago es de ConciliacionDePagos. Ninguna llamada al sistema de pago se hace con filas bloqueadas
@Slf4j
@Service
public class PagoService {

	// El largo de cobro.referencia: una más larga no puede ser nuestra
	private static final int LARGO_DE_LA_REFERENCIA = 120;

	private final PedidoService pedidoService;
	private final PedidoRepository pedidoRepository;
	private final CobroRepository cobroRepository;
	private final ConciliacionDePagos conciliacionDePagos;
	private final IPago iPago;
	private final Clock clock;
	private final String marca;
	// READ_COMMITTED: «Pagar» en dos pestañas se serializa con el bloqueo del cliente, y la segunda tiene que ver el
	// cobro que guardó la primera aunque algo se haya leído antes de bloquear (RF-2)
	private final TransactionTemplate transaccion;

	public PagoService(PedidoService pedidoService, PedidoRepository pedidoRepository, CobroRepository cobroRepository,
			ConciliacionDePagos conciliacionDePagos, IPago iPago, Clock clock,
			@Value("${tienda.correo.marca}") String marca, PlatformTransactionManager transactionManager) {
		this.pedidoService = pedidoService;
		this.pedidoRepository = pedidoRepository;
		this.cobroRepository = cobroRepository;
		this.conciliacionDePagos = conciliacionDePagos;
		this.iPago = iPago;
		this.clock = clock;
		this.marca = marca;
		this.transaccion = new TransactionTemplate(transactionManager);
		this.transaccion.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
	}

	// Un cobro por pedido (RF-1, RF-2): dos transacciones cortas y la llamada en el medio. Si mientras tanto otra
	// pestaña guardó su cobro, gana ese; el generado acá nunca se muestra, así que nadie lo paga
	public PedidoResponse pagar(Long idUsuario, Long idPedido) {
		Preparacion preparacion = transaccion.execute(estado -> preparar(idUsuario, idPedido));
		if (preparacion.existente() != null) {
			return preparacion.existente();
		}
		CobroGenerado generado;
		try {
			generado = iPago.generarCobro(idPedido, preparacion.monto(), preparacion.concepto());
		} catch (SistemaDePagoNoDisponibleException error) {
			log.warn("No se pudo generar el cobro del pedido {}", idPedido, error);
			throw new CobroNoDisponibleException(); // RF-3: no se guardó nada
		}
		return transaccion.execute(estado -> guardar(idUsuario, idPedido, generado));
	}

	// Pregunta al sistema de pago y devuelve cómo quedó el pedido (RF-4). Sin transacción propia: la conciliación
	// corre en la suya, y el pedido se lee después, ya confirmada. Al llegar a cero, el pedido vence acá si la
	// consulta dijo que no se pagó (RF-7)
	public PedidoResponse consultar(Long idUsuario, Long idPedido) {
		exigirDueño(idUsuario, idPedido);
		pedidoService.cobroPendiente(idPedido).map(Cobro::getIdCobro).ifPresent(conciliacionDePagos::conciliar);
		return pedidoService.ver(idUsuario, idPedido);
	}

	// El aviso del sistema de pago no confirma nada: adelanta la misma consulta (RF-10). Una referencia que no puede
	// ser nuestra no llega a consultar
	public void procesarAviso(String referencia) {
		if (referencia == null || referencia.isBlank() || referencia.length() > LARGO_DE_LA_REFERENCIA) {
			return;
		}
		cobroRepository.idPorReferencia(referencia).ifPresent(conciliacionDePagos::conciliar);
	}

	// Solo el dueño simula el pago de su cobro pendiente (RF-12, RF-14)
	@Transactional(readOnly = true)
	public String referenciaParaSimular(Long idUsuario, Long idPedido) {
		exigirDueño(idUsuario, idPedido);
		return pedidoService.cobroPendiente(idPedido).map(Cobro::getReferencia)
				.orElseThrow(CobroNoEncontradoException::new);
	}

	// Lo que hace falta para pedir el cobro, o el pedido con el cobro que ya tiene
	private record Preparacion(PedidoResponse existente, BigDecimal monto, String concepto) {
	}

	// Primera transacción: el cliente bloqueado antes de cualquier lectura, y el pedido todavía a tiempo
	private Preparacion preparar(Long idUsuario, Long idPedido) {
		LocalDateTime ahora = LocalDateTime.now(clock);
		Pedido pedido = pedidoService.bloquearPendiente(idUsuario, idPedido);
		if (pedidoService.cobroPendiente(idPedido).isPresent()) {
			return new Preparacion(pedidoService.respuesta(pedido, ahora), null, null);
		}
		exigirATiempo(pedido, ahora);
		return new Preparacion(null, pedido.getTotal(), "Pedido N.º " + idPedido + " — " + marca);
	}

	// Segunda transacción, con el cobro ya generado: el pedido pudo cancelarse, pasar la hora o recibir otro cobro
	private PedidoResponse guardar(Long idUsuario, Long idPedido, CobroGenerado generado) {
		LocalDateTime ahora = LocalDateTime.now(clock);
		Pedido pedido = pedidoService.bloquearPendiente(idUsuario, idPedido);
		if (pedidoService.cobroPendiente(idPedido).isEmpty()) {
			exigirATiempo(pedido, ahora);
			cobroRepository.save(Cobro.generar(pedido, generado.referencia(), generado.codigoQr(),
					generado.urlPasarela(), ahora));
		}
		return pedidoService.respuesta(pedido, ahora);
	}

	// Pasada la hora ya no se genera un cobro: el pedido vence en cuanto se lo mire
	private static void exigirATiempo(Pedido pedido, LocalDateTime ahora) {
		if (!pedido.getVence().isAfter(ahora)) {
			throw new PedidoNoPendienteException();
		}
	}

	// El pedido de otro cliente responde igual que uno inexistente (RF-14)
	private void exigirDueño(Long idUsuario, Long idPedido) {
		if (pedidoRepository.findByIdPedidoAndUsuario_IdUsuario(idPedido, idUsuario).isEmpty()) {
			throw new PedidoNoEncontradoException();
		}
	}
}
