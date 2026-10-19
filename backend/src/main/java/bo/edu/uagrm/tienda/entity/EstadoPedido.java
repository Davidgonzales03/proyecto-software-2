package bo.edu.uagrm.tienda.entity;

// Dónde está un pedido (HU-12): espera el pago hasta que se cancela o vence; HU-13 suma el pagado
public enum EstadoPedido {
	PENDIENTE_PAGO, CANCELADO, VENCIDO
}
