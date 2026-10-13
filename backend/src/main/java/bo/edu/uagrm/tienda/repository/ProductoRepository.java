package bo.edu.uagrm.tienda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import bo.edu.uagrm.tienda.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	// texto llega ya en minúsculas, con los comodines escapados con '!' y entre '%'. La categoría viene en la misma
	// consulta: la lista muestra su nombre
	@EntityGraph(attributePaths = "categoria")
	@Query("""
			select p from Producto p
			where (:idCategoria is null or p.categoria.idCategoria = :idCategoria)
			  and (:activo is null or p.activo = :activo)
			  and (:texto is null or lower(p.nombre) like :texto escape '!' or lower(p.codigo) like :texto escape '!'
			    or lower(p.marca) like :texto escape '!')
			""")
	Page<Producto> buscar(@Param("idCategoria") Long idCategoria, @Param("activo") Boolean activo,
			@Param("texto") String texto, Pageable paginacion);

	// El inventario (HU-08): activos e inactivos; texto como en buscar, en el nombre o el código
	@EntityGraph(attributePaths = "categoria")
	@Query("""
			select p from Producto p
			where (:idCategoria is null or p.categoria.idCategoria = :idCategoria)
			  and (:activo is null or p.activo = :activo)
			  and (:soloBajo = false or (p.stockMinimo is not null and p.stock <= p.stockMinimo))
			  and (:texto is null or lower(p.nombre) like :texto escape '!' or lower(p.codigo) like :texto escape '!')
			""")
	Page<Producto> inventario(@Param("texto") String texto, @Param("idCategoria") Long idCategoria,
			@Param("activo") Boolean activo, @Param("soloBajo") boolean soloBajo, Pageable paginacion);

	boolean existsByCategoria_IdCategoria(Long idCategoria);

	long countByCategoria_IdCategoria(Long idCategoria);

	// Cuántos productos tiene cada categoría, activos o no; las que no tienen ninguno no aparecen
	@Query("select p.categoria.idCategoria as idCategoria, count(p) as productos from Producto p group by p.categoria.idCategoria")
	List<ConteoPorCategoria> contarPorCategoria();

	// Serializa los cambios de imágenes del mismo producto: el quinto lugar no se ocupa dos veces, y ningún archivo
	// queda sin una fila que lo nombre
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Producto> findConBloqueoByIdProducto(Long idProducto);

	record ConteoPorCategoria(Long idCategoria, Long productos) {
	}
}
