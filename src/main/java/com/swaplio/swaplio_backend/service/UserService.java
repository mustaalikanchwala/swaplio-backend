// service/UserService.java
package com.swaplio.swaplio_backend.service;


import com.swaplio.swaplio_backend.dto.user.UpdateProfileRequest;
import com.swaplio.swaplio_backend.dto.user.UserProfileResponse;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setInstitution(request.institution());
        userRepository.save(user);

        return toResponse(user);
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .institution(user.getInstitution())
                .isVerified(user.isVerified())
                .build();
    }
}