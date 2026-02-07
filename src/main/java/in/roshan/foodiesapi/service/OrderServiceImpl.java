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
@AllArgsConstructor
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
        // Create order
        OrderEntity newOrder = convertToEntity(request);
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        newOrder.setUserId(userId);
        
        // Create dummy payment order
        Map<String, Object> paymentOrder = dummyPaymentService.createPaymentOrder(
            newOrder.getAmount(), 
            "INR"
        );
        
        newOrder.setRazorpayOrderId((String) paymentOrder.get("id"));
        newOrder.setPaymentStatus("created");
        newOrder = orderRepository.save(newOrder);
        
        return convertToResponse(newOrder);
    }

    @Override
    public void verifyPayment(Map<String, String> paymentData, String status) {
        String paymentOrderId = paymentData.get("razorpay_order_id");
        OrderEntity existingOrder = orderRepository.findByRazorpayOrderId(paymentOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        existingOrder.setPaymentStatus(status);
        existingOrder.setRazorpaySignature(paymentData.get("razorpay_signature"));
        existingOrder.setRazorpayPaymentId(paymentData.get("razorpay_payment_id"));
        orderRepository.save(existingOrder);
        
        if ("paid".equalsIgnoreCase(status)) {
            cartRespository.deleteByUserId(existingOrder.getUserId());
        }
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

    private OrderEntity convertToEntity(OrderRequest request) {
        return OrderEntity.builder()
                .userAddress(request.getUserAddress())
                .amount(request.getAmount())
                .orderedItems(request.getOrderedItems())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .orderStatus(request.getOrderStatus())
                .build();
    }
}
