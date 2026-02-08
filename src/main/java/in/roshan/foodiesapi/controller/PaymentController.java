package in.roshan.foodiesapi.controller;
import in.roshan.foodiesapi.io.PaymentRequest;
import in.roshan.foodiesapi.service.DummyPaymentService;
import in.roshan.foodiesapi.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@AllArgsConstructor
public class PaymentController {

    private final DummyPaymentService dummyPaymentService;
    private final OrderService orderService;

    // Create dummy payment order
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createPaymentOrder(@RequestBody Map<String, Object> request) {
        double amount = (Double) request.get("amount");
        String currency = (String) request.getOrDefault("currency", "INR");
        
        Map<String, Object> paymentOrder = dummyPaymentService.createPaymentOrder(amount, currency);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", paymentOrder,
            "message", "Dummy payment order created successfully"
        ));
    }

    // Process dummy payment
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody PaymentRequest paymentRequest) {
        Map<String, Object> paymentResult = dummyPaymentService.processPayment(
            paymentRequest.getPaymentOrderId(), 
            paymentRequest.getPaymentMethod()
        );
        
        // Update order status based on payment result
        String status = (String) paymentResult.get("status");
        Map<String, String> paymentData = Map.of(
            "razorpay_payment_id", (String) paymentResult.get("id"),
            "razorpay_order_id", paymentRequest.getPaymentOrderId()
        );
        
        orderService.verifyPayment(paymentData, status);
        
        return ResponseEntity.ok(Map.of(
            "success", "captured".equals(status),
            "data", paymentResult,
            "message", "Dummy payment processed successfully"
        ));
    }

    // Verify payment status
    @GetMapping("/verify/{paymentId}")
    public ResponseEntity<Map<String, Object>> verifyPayment(@PathVariable String paymentId) {
        boolean isValid = dummyPaymentService.verifyPayment(paymentId, "");
        DummyPaymentService.PaymentStatus status = dummyPaymentService.getPaymentStatus(paymentId);
        
        return ResponseEntity.ok(Map.of(
            "success", isValid,
            "status", status.toString(),
            "message", "Payment verification completed"
        ));
    }

    // Get payment methods (dummy)
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "card", Map.of(
                    "name", "Credit/Debit Card",
                    "icon", "💳",
                    "description", "Pay with Visa, Mastercard, Rupay"
                ),
                "upi", Map.of(
                    "name", "UPI",
                    "icon", "📱",
                    "description", "Pay with UPI apps"
                ),
                "netbanking", Map.of(
                    "name", "Net Banking",
                    "icon", "🏦",
                    "description", "Pay with your bank account"
                ),
                "wallet", Map.of(
                    "name", "Wallet",
                    "icon", "👛",
                    "description", "Pay with mobile wallets"
                )
            ),
            "message", "Available payment methods"
        ));
    }

    // Simulate payment failure (for testing)
    @PostMapping("/simulate-failure")
    public ResponseEntity<Map<String, Object>> simulatePaymentFailure(@RequestBody Map<String, String> request) {
        String orderId = request.get("orderId");
        
        return ResponseEntity.ok(Map.of(
            "success", false,
            "data", Map.of(
                "order_id", orderId,
                "status", "failed",
                "error_code", "BAD_REQUEST_ERROR",
                "error_description", "Payment failed for testing purposes"
            ),
            "message", "Payment failure simulated"
        ));
    }
}
