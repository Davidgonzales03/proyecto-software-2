package bo.edu.uagrm.tienda.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Lo que un Cliente confirmó de su carrito, para retirar en la tienda (HU-12). Espera el pago hasta que se paga
// (HU-13), vence a los 30 minutos o el cliente lo cancela; el stock lo reserva y lo devuelve PedidoService
@Entity
@Table(name = "pedido", indexes = {
		@Index(name = "idx_pedido_usuario_estado", columnList = "id_usuario, estado"),
		@Index(name = "idx_pedido_estado_vence", columnList = "estado, vence") })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pedido")
	private Long idPedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private EstadoPedido estado;

	// Hasta 50 líneas de 99 999,99 × 999
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@Column(name = "nombre_retira", nullable = false, length = 160)
	private String nombreRetira;

	@Column(name = "telefono_retira", nullable = false, length = 20)
	private String telefonoRetira;

	@Column(length = 200)
	private String nota;

	@Column(nullable = false)
	private LocalDateTime confirmado;

	@Column(nullable = false)
	private LocalDateTime vence;

	// Cuándo se canceló o venció
	private LocalDateTime cerrado;

	// Cuándo se pagó, también en un pago para devolver (HU-13)
	private LocalDateTime pagado;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
	@OrderBy("idLinea")
	private List<LineaPedido> lineas = new ArrayList<>();

	// Los datos de retiro ya validados (RF-6); se guardan sin los espacios de alrededor
	public static Pedido confirmar(Usuario usuario, String nombreRetira, String telefonoRetira, String nota,
			LocalDateTime confirmado, LocalDateTime vence) {
		Pedido pedido = new Pedido();
		pedido.usuario = Objects.requireNonNull(usuario);
		pedido.estado = EstadoPedido.PENDIENTE_PAGO;
		pedido.nombreRetira = nombreRetira.strip();
		pedido.telefonoRetira = telefonoRetira.strip();
		pedido.nota = nota == null || nota.isBlank() ? null : nota.strip();
		pedido.confirmado = Objects.requireNonNull(confirmado);
		pedido.vence = Objects.requireNonNull(vence);
		return pedido;
	}

	// Copia el producto como está ahora (RF-2)
	public void agregarLinea(Producto producto, BigDecimal cantidad) {
		LineaPedido linea = new LineaPedido(this, producto, cantidad);
		lineas.add(linea);
		total = total.add(linea.getSubtotal());
	}

	// Ordenadas por producto: el orden en que se bloquean los productos al liberar, el mismo que al confirmar
	public List<LineaPedido> lineasPorProducto() {
		return lineas.stream().sorted(Comparator.comparing(LineaPedido::getIdProducto)).toList();
	}

	public void cancelar(LocalDateTime fecha) {
		cerrar(EstadoPedido.CANCELADO, fecha);
	}

	public void vencer(LocalDateTime fecha) {
		cerrar(EstadoPedido.VENCIDO, fecha);
	}

	// Desde pendiente, o desde vencido o cancelado si es un pago tardío que revive (HU-13 RF-5, RF-9). La conciliación
	// lo revisa antes, con el pedido bloqueado
	public void pagar(LocalDateTime fecha) {
		if (estado != EstadoPedido.PENDIENTE_PAGO && !esPagoTardio()) {
			throw new IllegalStateException("El pedido " + idPedido + " no se puede pagar");
		}
		estado = EstadoPedido.PAGADO;
		pagado = Objects.requireNonNull(fecha);
	}

	// Un pago tardío sin stock: la devolución se hace fuera del sistema (HU-13 RF-9)
	public void marcarPagoADevolver(LocalDateTime fecha) {
		if (!esPagoTardio()) {
			throw new IllegalStateException("El pedido " + idPedido + " no tiene un pago tardío");
		}
		estado = EstadoPedido.PAGO_A_DEVOLVER;
		pagado = Objects.requireNonNull(fecha);
	}

	private boolean esPagoTardio() {
		return estado == EstadoPedido.VENCIDO || estado == EstadoPedido.CANCELADO;
	}

	// Solo desde «Pendiente de pago» (RF-12): PedidoService lo revisa antes, con el pedido bloqueado
	private void cerrar(EstadoPedido nuevo, LocalDateTime fecha) {
		if (estado != EstadoPedido.PENDIENTE_PAGO) {
			throw new IllegalStateException("El pedido " + idPedido + " ya no espera el pago");
		}
		estado = nuevo;
		cerrado = Objects.requireNonNull(fecha);
	}
}
