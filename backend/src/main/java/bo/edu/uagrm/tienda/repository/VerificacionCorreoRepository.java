package bo.edu.uagrm.tienda.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import bo.edu.uagrm.tienda.entity.Usuario;
import bo.edu.uagrm.tienda.entity.VerificacionCorreo;

public interface VerificacionCorreoRepository extends JpaRepository<VerificacionCorreo, Long> {

	Optional<VerificacionCorreo> findByHashToken(String hashToken);

	List<VerificacionCorreo> findByUsuarioAndUsadoFalse(Usuario usuario);
}
