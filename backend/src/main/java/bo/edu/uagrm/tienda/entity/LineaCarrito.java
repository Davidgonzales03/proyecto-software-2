package bo.edu.uagrm.tienda.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Un producto en el carrito de un cliente (HU-11). El carrito son las líneas del cliente, una por producto; no guarda
// el precio, que siempre es el de hoy (RF-8)
@Entity
@Table(name = "linea_carrito", uniqueConstraints = @UniqueConstraint(name = "uk_linea_carrito_usuario_producto",
		columnNames = { "id_usuario", "id_producto" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineaCarrito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_linea")
	private Long idLinea;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_producto", nullable = false)
	private Producto producto;

	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal cantidad;

	@Column(name = "fecha_agregada", nullable = false)
	private LocalDateTime fechaAgregada;

	public static LineaCarrito crear(Usuario usuario, Producto producto, BigDecimal cantidad,
			LocalDateTime fechaAgregada) {
		LineaCarrito linea = new LineaCarrito();
		linea.usuario = Objects.requireNonNull(usuario);
		linea.producto = Objects.requireNonNull(producto);
		linea.fechaAgregada = Objects.requireNonNull(fechaAgregada);
		linea.cambiar(cantidad);
		return linea;
	}

	// Las reglas de la cantidad las revisa CarritoService; acá solo se guarda con la escala de la columna
	public void cambiar(BigDecimal cantidad) {
		this.cantidad = cantidad.setScale(2, RoundingMode.UNNECESSARY);
	}
}
