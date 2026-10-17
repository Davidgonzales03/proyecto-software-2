package bo.edu.uagrm.tienda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import bo.edu.uagrm.tienda.entity.LineaCarrito;

public interface LineaCarritoRepository extends JpaRepository<LineaCarrito, Long> {

	// El carrito de un cliente, en el orden en que agregó cada producto (HU-11 RF-8). El producto y su categoría vienen
	// en la misma consulta: la respuesta dice si cada uno se puede comprar
	@EntityGraph(attributePaths = { "producto", "producto.categoria" })
	List<LineaCarrito> findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(Long idUsuario);

	Optional<LineaCarrito> findByUsuario_IdUsuarioAndProducto_IdProducto(Long idUsuario, Long idProducto);

	long countByUsuario_IdUsuario(Long idUsuario);

	void deleteByUsuario_IdUsuario(Long idUsuario);
}
