package bo.edu.uagrm.tienda.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import bo.edu.uagrm.tienda.entity.MovimientoStock;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

	// El historial muestra quién hizo cada movimiento: el usuario viene en la misma consulta
	@EntityGraph(attributePaths = "usuario")
	Page<MovimientoStock> findByProducto_IdProducto(Long idProducto, Pageable paginacion);
}
