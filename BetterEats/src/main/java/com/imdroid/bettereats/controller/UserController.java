package com.imdroid.bettereats.controller;

import com.imdroid.bettereats.exception.ResourceNotFoundException;
import com.imdroid.bettereats.model.User;
import com.imdroid.bettereats.payload.*;
import com.imdroid.bettereats.repository.EntreeRepository;
import com.imdroid.bettereats.repository.UserRepository;
import com.imdroid.bettereats.repository.ReviewToUserRepository;
import com.imdroid.bettereats.security.UserPrincipal;
import com.imdroid.bettereats.service.EntreeService;
import com.imdroid.bettereats.security.CurrentUser;
import com.imdroid.bettereats.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntreeService entreeService;


    @GetMapping("/user/me")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {

        UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
        List<String> roles = currentUser.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        userSummary.setRole(roles.get(0));
        return userSummary;
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));


        UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt());

        return userProfile;
    }

    @GetMapping("/users/{username}/reviews")
    public PagedResponse<ReviewResponse> getReviewsCreatedBy(@PathVariable(value = "username") String username,
                                                             @CurrentUser UserPrincipal currentUser,
                                                             @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                             @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return entreeService.getReviewsCreatedBy(username, currentUser, page, size);
    }

}