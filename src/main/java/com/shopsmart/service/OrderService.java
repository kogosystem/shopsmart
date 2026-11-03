package com.shopsmart.service;

import com.shopsmart.dto.CheckoutForm;
import com.shopsmart.entity.*;
import com.shopsmart.exception.BusinessException;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order processing. The whole of placeOrder() runs inside ONE database
 * transaction (the @Transactional annotation): Spring opens a transaction when
 * the method starts and COMMITS it only if the method returns normally. If any
 * line throws - for example a stock check fails - Spring ROLLS BACK everything,
 * so you can never end up with an order saved but stock not reduced, or stock
 * reduced but the cart not cleared. That is exactly the all-or-nothing
 * behaviour the spec asked for.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order placeOrder(User customer, CheckoutForm form) {
        // 1) Validate the cart exists and is not empty.
        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new BusinessException("Your cart is empty."));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Your cart is empty.");
        }

        // 2) Build the order shell with the delivery snapshot from the form.
        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(form.getPaymentMethod());
        // Cash on delivery stays PENDING; the M-Pesa SIMULATION is marked PAID
        // only to demonstrate the flow - no real money moves (spec section 94).
        order.setPaymentStatus(form.getPaymentMethod() == PaymentMethod.MPESA_SIMULATION
                ? PaymentStatus.PAID : PaymentStatus.PENDING);
        order.setDeliveryName(form.getDeliveryName());
        order.setDeliveryPhone(form.getDeliveryPhone());
        order.setDeliveryAddress(form.getDeliveryAddress());
        order.setDeliveryCity(form.getDeliveryCity());

        BigDecimal total = BigDecimal.ZERO;

        // 3) For each cart line: re-check stock and re-read the price FROM THE DB
        //    (never trust a price sent by the browser), then build an order item.
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("A product in your cart no longer exists."));

            if (!product.isActive()) {
                throw new BusinessException(product.getName() + " is no longer available.");
            }
            if (cartItem.getQuantity() > product.getStock()) {
                throw new BusinessException("Not enough stock for " + product.getName()
                        + " (only " + product.getStock() + " left).");
            }

            BigDecimal unitPrice = product.getPrice();                       // trusted DB price
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());                     // snapshot
            orderItem.setCategoryId(product.getCategory().getId());          // snapshot
            orderItem.setUnitPrice(unitPrice);                               // snapshot
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setLineTotal(lineTotal);
            order.addItem(orderItem);

            // 4) Reduce stock on the product.
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 5) Server-calculated total, then save the order (cascade saves items too).
        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // 6) Clear the cart. order_items are now the permanent purchase history.
        cart.getItems().clear();
        cartRepository.save(cart);

        // 7) Returning normally lets Spring COMMIT the whole transaction.
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    /** An order the given customer is allowed to see (their own order only). */
    @Transactional(readOnly = true)
    public Order getOwnedBy(Long orderId, Long customerId) {
        Order order = getById(orderId);
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException("That order is not yours.");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> ordersForCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> recentForCustomer(Long customerId, int limit) {
        return orderRepository.findRecentByCustomer(customerId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public Page<Order> allOrders(int page, int size) {
        return orderRepository.findAllByOrderByOrderDateDesc(PageRequest.of(page, size));
    }

    /** Admin changes an order's status (PENDING -> CONFIRMED -> ... -> DELIVERED). */
    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = getById(orderId);
        order.setStatus(status);
        // When an order is delivered and paid cash on delivery, mark it paid.
        if (status == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        orderRepository.save(order);
    }

    // ---- dashboard figures ----
    @Transactional(readOnly = true)
    public long countOrders() { return orderRepository.countSellableOrders(); }

    @Transactional(readOnly = true)
    public BigDecimal totalSales() { return orderRepository.totalSales(); }

    @Transactional(readOnly = true)
    public BigDecimal averageOrderValue() { return orderRepository.averageOrderValue(); }
}
