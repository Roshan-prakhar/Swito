package in.roshan.foodiesapi.service;

import in.roshan.foodiesapi.io.UserRequest;
import in.roshan.foodiesapi.io.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    String findByUserId();
}
