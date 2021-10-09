package com.imdroid.bettereats.controller;

import com.imdroid.bettereats.model.Entree;
import com.imdroid.bettereats.model.Restaurant;
import com.imdroid.bettereats.payload.*;
import com.imdroid.bettereats.service.RestaurantService;
import com.imdroid.bettereats.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @GetMapping
    public PagedResponse<RestaurantResponse> getRestaurants(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return restaurantService.getAllRestaurants(page, size);
    }

    @PostMapping
//    @PreAuthorize("hasRole('USER')")
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest restaurantRequest) {
        Restaurant restaurant = restaurantService.createRestaurant(restaurantRequest);

        return restaurantService.getRestaurantById(restaurant.getId());
    }

    @GetMapping("/{restaurantId}")
    public RestaurantResponse getRestaurantById(@PathVariable Long restaurantId) {
        return restaurantService.getRestaurantById(restaurantId);
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<?> deleteRestaurantById(@PathVariable Long restaurantId) {
        restaurantService.deleteRestaurantById(restaurantId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        // return empty list to remove firefox empty xml error
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @GetMapping("/{restaurantId}/entrees")
    public PagedResponse<EntreeResponse> getEntreesByRestaurantId(
            @PathVariable Long restaurantId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return restaurantService.getEntreesByRestaurantId(restaurantId, page, size);
    }

    @PostMapping("/{restaurantId}/entree")  
    public ResponseEntity<?> createEntree(
            @PathVariable Long restaurantId,
            @Valid @RequestBody EntreeRequest entreeRequest) {
        Entree entree = restaurantService.createEntree(restaurantId, entreeRequest);

        URI location = ServletUriComponentsBuilder
                .fromPath("/entrees").path("/{entreeId}")
                .buildAndExpand(entree.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Entree Created Successfully"));
    }

}