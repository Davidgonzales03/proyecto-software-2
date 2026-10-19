package bo.edu.uagrm.tienda.dto;

// El pedido cancelado y cuántas de sus líneas no volvieron al carrito porque ya tenía 50 (HU-12 RF-9, RF-16)
public record CancelacionResponse(PedidoResponse pedido, int lineasQueNoVolvieron) {
}
