package bo.edu.uagrm.tienda.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// El cobro de un pedido en el sistema de pago, con su QR (HU-13; decisiones.md → Pagos). Puede haber varios por pedido,
// porque después de un error se genera otro, pero nunca dos pendientes a la vez: PagoService no genera otro mientras
// haya uno pendiente ni con el pedido pagado, así que uno solo puede quedar pagado
@Entity
@Table(name = "cobro", indexes = {
		@Index(name = "idx_cobro_pedido_estado", columnList = "id_pedido, estado"),
		@Index(name = "idx_cobro_estado_generado", columnList = "estado, generado") })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cobro {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_cobro")
	private Long idCobro;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	// El total del pedido, nunca uno que mande el cliente
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	// Nuestro identificador en el sistema de pago: con él se consulta y llega el aviso
	@Column(nullable = false, length = 120, unique = true)
	private String referencia;

	// El texto de un QR interoperable puede pasar de 255 caracteres
	@Column(name = "codigo_qr", nullable = false, length = 1000)
	private String codigoQr;

	@Column(name = "url_pasarela", length = 255)
	private String urlPasarela;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private EstadoCobro estado;

	@Column(nullable = false)
	private LocalDateTime generado;

	@Column(name = "fecha_pago")
	private LocalDateTime fechaPago;

	// La última vez que el sistema de pago dijo que no estaba pagado: si fue después de la hora, el pedido puede
	// vencer (RF-7)
	@Column(name = "consultado_sin_pago")
	private LocalDateTime consultadoSinPago;

	public static Cobro generar(Pedido pedido, String referencia, String codigoQr, String urlPasarela,
			LocalDateTime generado) {
		Cobro cobro = new Cobro();
		cobro.pedido = Objects.requireNonNull(pedido);
		cobro.monto = pedido.getTotal();
		cobro.referencia = Objects.requireNonNull(referencia);
		cobro.codigoQr = Objects.requireNonNull(codigoQr);
		cobro.urlPasarela = urlPasarela;
		cobro.estado = EstadoCobro.PENDIENTE;
		cobro.generado = Objects.requireNonNull(generado);
		return cobro;
	}

	public void registrarPago(LocalDateTime fecha) {
		exigirPendiente();
		estado = EstadoCobro.PAGADO;
		fechaPago = Objects.requireNonNull(fecha);
	}

	// RF-6: el pedido sigue igual y se puede generar otro
	public void registrarError() {
		exigirPendiente();
		estado = EstadoCobro.ERROR;
	}

	public void consultadoSinPago(LocalDateTime fecha) {
		exigirPendiente();
		consultadoSinPago = Objects.requireNonNull(fecha);
	}

	// Un resultado se aplica una sola vez (RF-11): ConciliacionDePagos lo revisa antes, con el cobro bloqueado
	private void exigirPendiente() {
		if (estado != EstadoCobro.PENDIENTE) {
			throw new IllegalStateException("El cobro " + idCobro + " ya fue procesado");
		}
	}
}
