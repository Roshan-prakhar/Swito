package in.roshan.foodiesapi.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    
    private String paymentOrderId;
    private String paymentMethod;
    private Double amount;
    private String currency;
}
