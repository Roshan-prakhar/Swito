package in.roshan.foodiesapi.service;

import in.roshan.foodiesapi.entity.CartEntity;
import in.roshan.foodiesapi.io.CartRequest;
import in.roshan.foodiesapi.io.CartResponse;
import in.roshan.foodiesapi.repository.CartRespository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService{

    private final CartRespository cartRespository;
    private final UserService userService;
    @Override
    public CartResponse addToCart(CartRequest request) {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        Optional<CartEntity> cartOptional = cartRespository.findByUserId(userId);
        CartEntity cart = cartOptional.orElseGet(() -> new CartEntity(userId, new HashMap<>()));
        Map<String, Integer> cartItems = cart.getItems();
        cartItems.put(request.getFoodId(), cartItems.getOrDefault(request.getFoodId(), 0) + 1);
        cart.setItems(cartItems);
        cart = cartRespository.save(cart);
        return convertToResponse(cart);
    }

    @Override
    public CartResponse getCart() {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        CartEntity entity = cartRespository.findByUserId(userId)
                .orElse(new CartEntity(null, userId, new HashMap<>()));
        return convertToResponse(entity);
    }

    @Override
    public void clearCart() {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        cartRespository.deleteByUserId(userId);
    }

    @Override
    public CartResponse removeFromCart(CartRequest cartRequest) {
        String loggedInUserId = userService.findByUserId();
        Long userId = Long.parseLong(loggedInUserId);
        CartEntity entity = cartRespository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is not found"));
        Map<String, Integer> cartItems = entity.getItems();
        if (cartItems.containsKey(cartRequest.getFoodId())) {
            int currentQty = cartItems.get(cartRequest.getFoodId());
            if (currentQty > 0) {
                cartItems.put(cartRequest.getFoodId(), currentQty - 1);
            } else {
                cartItems.remove(cartRequest.getFoodId());
            }
            entity = cartRespository.save(entity);
        }
        return convertToResponse(entity);
    }

    private CartResponse convertToResponse(CartEntity cartEntity) {
        return CartResponse.builder()
                .id(cartEntity.getId().toString())
                .userId(cartEntity.getUserId().toString())
                .items(cartEntity.getItems())
                .build();
    }
}
