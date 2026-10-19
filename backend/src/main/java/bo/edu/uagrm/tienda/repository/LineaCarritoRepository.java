package bo.edu.uagrm.tienda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import bo.edu.uagrm.tienda.entity.LineaCarrito;

public interface LineaCarritoRepository extends JpaRepository<LineaCarrito, Long> {

	// El carrito de un cliente, en el orden en que agregó cada producto (HU-11 RF-8). El producto y su categoría vienen
	// en la misma consulta: la respuesta dice si cada uno se puede comprar
	@EntityGraph(attributePaths = { "producto", "producto.categoria" })
	List<LineaCarrito> findByUsuario_IdUsuarioOrderByFechaAgregadaAscIdLineaAsc(Long idUsuario);

	// Solo los ids, por id ascendente: el orden en que la confirmación bloquea los productos (HU-12 RF-7)
	@Query("select l.producto.idProducto from LineaCarrito l where l.usuario.idUsuario = :idUsuario order by l.producto.idProducto")
	List<Long> idsDeProductos(@Param("idUsuario") Long idUsuario);

	Optional<LineaCarrito> findByUsuario_IdUsuarioAndProducto_IdProducto(Long idUsuario, Long idProducto);

	long countByUsuario_IdUsuario(Long idUsuario);

	void deleteByUsuario_IdUsuario(Long idUsuario);
}
