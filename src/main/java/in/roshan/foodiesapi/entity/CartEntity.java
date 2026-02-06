package in.roshan.foodiesapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "carts")
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @ElementCollection
@CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
@MapKeyColumn(name = "food_id")
@Column(name = "quantity")
private Map<String, Integer> items = new HashMap<>();

    public CartEntity(Long userId, Map<String, Integer> items) {
        this.userId = userId;
        this.items = items;
    }
}
