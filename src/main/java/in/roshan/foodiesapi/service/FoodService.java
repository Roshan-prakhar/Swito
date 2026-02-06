package in.roshan.foodiesapi.service;

import in.roshan.foodiesapi.io.FoodRequest;
import in.roshan.foodiesapi.io.FoodResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FoodService {

    String uploadFile(MultipartFile file);

    FoodResponse addFood(FoodRequest request, MultipartFile file);

    List<FoodResponse> readFoods();

    FoodResponse readFood(Long id);

    boolean deleteFile(String filename);

    void deleteFood(Long id);
}
