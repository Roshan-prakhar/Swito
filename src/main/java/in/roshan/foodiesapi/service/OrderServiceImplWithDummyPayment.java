package in.roshan.foodiesapi.service;

import in.roshan.foodiesapi.entity.OrderEntity;
import in.roshan.foodiesapi.io.OrderRequest;
import in.roshan.foodiesapi.io.OrderResponse;
import in.roshan.foodiesapi.repository.CartRespository;
import in.roshan.foodiesapi.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CartRespository cartRespository;
    @Autowired
    private DummyPaymentService dummyPaymentService;

    @Override
    public OrderResponse createOrderWithPayment(OrderRequest request) {
        OrderEntity newOrder = convertToEntity(request);
        newOrder = orderRepository.save(newOrder);

        // Create dummy payment order
        Map<String, Object> paymentOrder = dummyPaymentService.createPaymentOrder(
            newOrder.getAmount(), "INR"
        );
        
        newOrder.setRazorpayOrderId((String) paymentOrder.get("id"));
        newOrder.setPaymentStatus("PENDING");
        newOrder.setOrderStatus("PLACED");
        
        newOrder = orderRepository.save(newOrder);
        
        // Clear the cart after order is placed
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        cartRespository.deleteByUserId(userId);
        
        return convertToResponse(newOrder);
    }

    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String paymentId = paymentData.get("razorpay_payment_id");
        String orderId = paymentData.get("razorpay_order_id");
        
        // Find the order
        OrderEntity order = orderRepository.findByRazorpayOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify dummy payment
        boolean isVerified = dummyPaymentService.verifyPayment(paymentId, orderId);
        
        if (isVerified && "captured".equals(status)) {
            order.setPaymentStatus("COMPLETED");
            order.setOrderStatus("CONFIRMED");
        } else {
            order.setPaymentStatus("FAILED");
            order.setOrderStatus("CANCELLED");
        }
        
        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getUserOrders() {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        List<OrderEntity> list = orderRepository.findByUserId(userId);
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    @Override
    public void removeOrder(String orderId) {
        Long id = Long.parseLong(orderId);
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderResponse> getOrdersOfAllUsers() {
        List<OrderEntity> list = orderRepository.findAll();
        return list.stream().map(entity -> convertToResponse(entity)).collect(Collectors.toList());
    }

    @Override
    public void updateOrderStatus(String orderId, String status) {
        Long id = Long.parseLong(orderId);
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        entity.setOrderStatus(status);
        orderRepository.save(entity);
    }

    // New method for processing dummy payment
    public PaymentResponse processDummyPayment(PaymentRequest paymentRequest) {
        Map<String, Object> paymentResult = dummyPaymentService.processPayment(
            paymentRequest.getPaymentOrderId(), 
            paymentRequest.getPaymentMethod()
        );
        
        return PaymentResponse.builder()
            .id((String) paymentResult.get("id"))
            .entity((String) paymentResult.get("entity"))
            .amount((Integer) paymentResult.get("amount"))
            .currency((String) paymentResult.get("currency"))
            .status((String) paymentResult.get("status"))
            .method((String) paymentResult.get("method"))
            .orderId((String) paymentResult.get("order_id"))
            .description((String) paymentResult.get("description"))
            .errorCode((String) paymentResult.get("error_code"))
            .errorDescription((String) paymentResult.get("error_description"))
            .build();
    }

    private OrderEntity convertToEntity(OrderRequest request) {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        
        return OrderEntity.builder()
                .userId(userId)
                .amount(request.getAmount())
                .userAddress(request.getUserAddress())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .orderedItems(request.getOrderedItems())
                .build();
    }

    private OrderResponse convertToResponse(OrderEntity newOrder) {
        return OrderResponse.builder()
                .id(newOrder.getId().toString())
                .amount(newOrder.getAmount())
                .userAddress(newOrder.getUserAddress())
                .userId(newOrder.getUserId().toString())
                .razorpayOrderId(newOrder.getRazorpayOrderId())
                .paymentStatus(newOrder.getPaymentStatus())
                .orderStatus(newOrder.getOrderStatus())
                .email(newOrder.getEmail())
                .phoneNumber(newOrder.getPhoneNumber())
                .orderedItems(newOrder.getOrderedItems())
                .build();
    }
}
