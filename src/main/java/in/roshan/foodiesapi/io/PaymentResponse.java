package in.roshan.foodiesapi.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    
    private String id;
    private String entity;
    private Integer amount;
    private String currency;
    private String status;
    private String method;
    private String orderId;
    private String description;
    private CardDetails card;
    private String errorCode;
    private String errorDescription;
    
    @Data
    @Builder
    public static class CardDetails {
        private String last4;
        private String network;
        private String type;
    }
}
