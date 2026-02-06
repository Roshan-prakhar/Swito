package in.roshan.foodiesapi.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DummyPaymentService {

    // Simulate different payment scenarios
    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    // Create a dummy payment order
    public Map<String, Object> createPaymentOrder(double amount, String currency) {
        String orderId = "DUMMY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Map<String, Object> paymentOrder = new HashMap<>();
        paymentOrder.put("id", orderId);
        paymentOrder.put("entity", "order");
        paymentOrder.put("amount", amount * 100); // Convert to paise/cents
        paymentOrder.put("currency", currency);
        paymentOrder.put("status", "created");
        paymentOrder.put("payment_capture", 1);
        
        // Add dummy payment methods
        paymentOrder.put("methods", Map.of(
            "card", true,
            "upi", true,
            "netbanking", true,
            "wallet", true
        ));
        
        return paymentOrder;
    }

    // Process dummy payment (simulates payment gateway response)
    public Map<String, Object> processPayment(String paymentOrderId, String paymentMethod) {
        Map<String, Object> paymentResponse = new HashMap<>();
        
        // Simulate different payment scenarios based on order ID
        // For study purposes, we'll make 90% of payments successful
        boolean isSuccess = paymentOrderId.hashCode() % 10 != 0; // 90% success rate
        
        if (isSuccess) {
            paymentResponse.put("id", "pay_" + UUID.randomUUID().toString().substring(0, 12));
            paymentResponse.put("entity", "payment");
            paymentResponse.put("amount", 10000); // Dummy amount
            paymentResponse.put("currency", "INR");
            paymentResponse.put("status", "captured");
            paymentResponse.put("method", paymentMethod);
            paymentResponse.put("order_id", paymentOrderId);
            paymentResponse.put("description", "Dummy Payment Successful");
            
            // Add dummy card details (masked)
            if ("card".equals(paymentMethod)) {
                paymentResponse.put("card", Map.of(
                    "last4", "1111",
                    "network", "Visa",
                    "type", "debit"
                ));
            }
        } else {
            paymentResponse.put("id", "pay_" + UUID.randomUUID().toString().substring(0, 12));
            paymentResponse.put("entity", "payment");
            paymentResponse.put("amount", 10000);
            paymentResponse.put("currency", "INR");
            paymentResponse.put("status", "failed");
            paymentResponse.put("method", paymentMethod);
            paymentResponse.put("order_id", paymentOrderId);
            paymentResponse.put("description", "Dummy Payment Failed");
            paymentResponse.put("error_code", "BAD_REQUEST_ERROR");
            paymentResponse.put("error_description", "The payment could not be processed");
        }
        
        return paymentResponse;
    }

    // Verify dummy payment
    public boolean verifyPayment(String paymentId, String orderId) {
        // For study purposes, always return true if paymentId exists
        return paymentId != null && paymentId.startsWith("pay_");
    }

    // Get payment status
    public PaymentStatus getPaymentStatus(String paymentId) {
        // Simulate payment status check
        if (paymentId == null) return PaymentStatus.FAILED;
        
        // 90% success rate for demo
        return paymentId.hashCode() % 10 != 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    // Refund dummy payment
    public Map<String, Object> refundPayment(String paymentId, double amount) {
        Map<String, Object> refundResponse = new HashMap<>();
        
        refundResponse.put("id", "refund_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        refundResponse.put("entity", "refund");
        refundResponse.put("amount", amount * 100);
        refundResponse.put("currency", "INR");
        refundResponse.put("payment_id", paymentId);
        refundResponse.put("status", "processed");
        refundResponse.put("description", "Dummy Refund Processed");
        
        return refundResponse;
    }
}
