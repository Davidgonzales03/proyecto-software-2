package bo.edu.uagrm.tienda.dto.validacion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

// La cantidad de una línea del carrito (HU-11 RF-3), en un solo lugar para agregar y para cambiarla: mayor que 0,
// hasta 999 y con dos decimales como máximo. Los decimales según la unidad los revisa CarritoService
@NotNull(message = "es obligatoria")
@DecimalMin(value = "0", inclusive = false, message = "tiene que ser mayor que 0")
@DecimalMax(value = CantidadDeCarrito.MAXIMA, message = "admite hasta " + CantidadDeCarrito.MAXIMA)
@Digits(integer = 3, fraction = 2, message = "admite hasta dos decimales")
@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CantidadDeCarrito {

	String MAXIMA = "999";

	String message() default "";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
