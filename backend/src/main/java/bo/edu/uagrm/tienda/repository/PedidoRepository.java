package bo.edu.uagrm.tienda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bo.edu.uagrm.tienda.entity.EstadoPedido;
import bo.edu.uagrm.tienda.entity.Pedido;
import jakarta.persistence.LockModeType;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	// El de otro cliente no aparece: responde igual que uno que no existe (HU-12 RF-13)
	Optional<Pedido> findByIdPedidoAndUsuario_IdUsuario(Long idPedido, Long idUsuario);

	// Cancelar y vencer revisan el estado con el pedido bloqueado: el que llega segundo no libera dos veces (RF-12)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Pedido> findConBloqueoByIdPedidoAndUsuario_IdUsuario(Long idPedido, Long idUsuario);

	// Un solo pedido pendiente de pago por cliente (RF-5)
	Optional<Pedido> findFirstByUsuario_IdUsuarioAndEstado(Long idUsuario, EstadoPedido estado);

	// Los que ya cumplieron la hora, para la tarea programada (RF-10): solo los ids, y cada uno vence en su transacción
	@Query("""
			select new bo.edu.uagrm.tienda.repository.PedidoRepository$PedidoAVencer(p.idPedido, p.usuario.idUsuario)
			from Pedido p where p.estado = :estado and p.vence <= :ahora order by p.vence, p.idPedido""")
	List<PedidoAVencer> findByEstadoAndVenceLessThanEqual(@Param("estado") EstadoPedido estado,
			@Param("ahora") LocalDateTime ahora);

	record PedidoAVencer(Long idPedido, Long idUsuario) {
	}
}
