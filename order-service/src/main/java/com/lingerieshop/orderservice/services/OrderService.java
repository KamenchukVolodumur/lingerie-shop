package com.lingerieshop.orderservice.services;

import com.lingerieshop.orderservice.clients.CartServiceClient;
import com.lingerieshop.orderservice.clients.PaymentServiceClient;
import com.lingerieshop.orderservice.clients.ProductServiceClient;
import com.lingerieshop.orderservice.dto.CartResponseDTO;
import com.lingerieshop.orderservice.dto.CreateOrderRequestDTO;
import com.lingerieshop.orderservice.dto.DeductStockRequestDTO;
import com.lingerieshop.orderservice.dto.OrderItemResponseDTO;
import com.lingerieshop.orderservice.dto.OrderResponseDTO;
import com.lingerieshop.orderservice.dto.PaymentLinkResponseDTO;
import com.lingerieshop.orderservice.dto.PaymentRequestDTO;
import com.lingerieshop.orderservice.entities.Order;
import com.lingerieshop.orderservice.entities.OrderItem;
import com.lingerieshop.orderservice.entities.enums.OrderStatus;
import com.lingerieshop.orderservice.entities.enums.PaymentMethod;
import com.lingerieshop.orderservice.entities.enums.PaymentStatus;
import com.lingerieshop.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest) {
        // TODO: заглушка — коли з'явиться auth-service, брати userId з токена
        Long userId = createOrderRequest.userId() != null ? createOrderRequest.userId() : 1L;

        CartResponseDTO cart = cartServiceClient.getCart(createOrderRequest.cartId());
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalStateException("Cart is empty: " + createOrderRequest.cartId());
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setStatus(OrderStatus.NEW);
        order.setPaymentMethod(createOrderRequest.paymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        
        String providerName = createOrderRequest.deliveryProvider().equals("NOVA_POSHTA") ? "Нова Пошта" : "Укрпошта";
        order.setShippingAddress(providerName + ": " + createOrderRequest.deliveryCity() + ", " + createOrderRequest.deliveryBranch());
        
        order.setShippingPhone(createOrderRequest.shippingPhone());
        // TODO: заглушка — потім рахувати реальну вартість доставки
        order.setShippingCost(BigDecimal.valueOf(100));

        List<OrderItem> items = cart.items().stream()
                .map(cartItem -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductId(cartItem.productId());
                    item.setProductVariantId(cartItem.productVariantId());
                    item.setQuantity(cartItem.quantity());
                    item.setProductName(cartItem.productName());
                    item.setVariantSize(cartItem.size());
                    item.setVariantColor(cartItem.color());
                    item.setPricePerUnit(cartItem.unitPrice());
                    item.setTotalPrice(cartItem.itemTotal());
                    return item;
                })
                .collect(Collectors.toList());
        order.setItems(items);
        order.setTotalAmount(cart.totalSum().add(order.getShippingCost()));

        Order saved = orderRepository.save(order);
        String paymentUrl = null;

        if (saved.getPaymentMethod() == PaymentMethod.CARD) {
            PaymentLinkResponseDTO paymentLink = paymentServiceClient.generatePaymentLink(
                    new PaymentRequestDTO(saved.getId(), saved.getTotalAmount()));
            paymentUrl = paymentLink.paymentUrl();
        }

//        List<DeductStockRequestDTO> deductRequests = items.stream()
//                .map(i -> new DeductStockRequestDTO(i.getProductVariantId(), i.getQuantity()))
//                .toList();
//        productServiceClient.deductStock(deductRequests);

        cartServiceClient.clearCart(createOrderRequest.cartId());

        return mapToDTO(saved, paymentUrl);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        return mapToDTO(findOrderById(orderId));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public OrderResponseDTO confirmOrder(Long orderId) {          // NEW -> CONFIRMED
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException(
                    "Only NEW orders can be confirmed, current status: " + order.getStatus());
        }

        List<DeductStockRequestDTO> stockAdjustments = order.getItems().stream()
                .map(item -> new DeductStockRequestDTO(item.getProductVariantId(), item.getQuantity()))
                .toList();
        productServiceClient.deductStock(stockAdjustments);

        try {
            if (order.getPaymentMethod() == PaymentMethod.CARD) {
                paymentServiceClient.capturePayment(orderId);
                order.setPaymentStatus(PaymentStatus.PAID);
            }
        } catch (RuntimeException exception) {
            productServiceClient.releaseStock(stockAdjustments);
            throw exception;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO markConfirmationEmailSent(Long orderId) {
        Order order = findOrderById(orderId);
        order.setConfirmationEmailSentAt(LocalDateTime.now());
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO markAsPaid(Long orderId) {            // PENDING -> PAID
        Order order = findOrderById(orderId);
        if (order.getPaymentMethod() != PaymentMethod.CASH_ON_DELIVERY) {
            throw new IllegalStateException("Card payments are captured when the order is confirmed");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING payments can be marked as PAID, current status: " + order.getPaymentStatus());
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public void markPaymentFailed(Long orderId) {
        Order order = findOrderById(orderId);
        if (order.getStatus() == OrderStatus.NEW) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.CANCELLED);
        }
    }

    @Transactional
    public OrderResponseDTO markAsShipped(Long orderId) {         // CONFIRMED -> SHIPPED
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED orders can be shipped, current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.SHIPPED);
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO completeOrder(Long orderId) {         // SHIPPED -> COMPLETED
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException(
                    "Only SHIPPED orders can be completed, current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.COMPLETED);
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO revertOrder(Long orderId) {
        Order order = findOrderById(orderId);
        
        switch (order.getStatus()) {
            case COMPLETED:
                order.setStatus(OrderStatus.SHIPPED);
                break;
            case SHIPPED:
                order.setStatus(OrderStatus.CONFIRMED);
                break;
            case CONFIRMED:
                order.setStatus(OrderStatus.NEW);
                break;
            case CANCELLED:
                throw new IllegalStateException("Cannot revert a CANCELLED order (inventory was already refunded).");
            case NEW:
                throw new IllegalStateException("Order is already at the first status (NEW).");
        }
        
        return mapToDTO(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {           // NEW/CONFIRMED -> CANCELLED
        Order order = findOrderById(orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new IllegalStateException("Only NEW orders can be canceled, current status: " + order.getStatus());
        }

        if (order.getPaymentMethod() == PaymentMethod.CARD) {
            paymentServiceClient.cancelPayment(orderId);
            order.setPaymentStatus(PaymentStatus.CANCELED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return mapToDTO(orderRepository.save(order));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public OrderResponseDTO mapToDTO(Order order) {
        return mapToDTO(order, null);
    }

    private OrderResponseDTO mapToDTO(Order order, String paymentUrl) {
        List<OrderItemResponseDTO> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                        .map(this::mapItemToDTO)
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getUserId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getShippingCost(),
                order.getShippingAddress(),
                order.getShippingPhone(),
                items,
                order.getConfirmationEmailSentAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                paymentUrl
        );
    }

    private OrderItemResponseDTO mapItemToDTO(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getId(),
                item.getProductId(),
                item.getProductVariantId(),
                item.getQuantity(),
                item.getProductName(),
                item.getVariantSize(),
                item.getVariantColor(),
                item.getPricePerUnit(),
                item.getTotalPrice()
        );
    }
}
