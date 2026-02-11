package kn.org.deliverybackend.controller;

import kn.org.deliverybackend.dto.UsersDTO;
import kn.org.deliverybackend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/app/consumer")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UsersDTO> getProfile(@PathVariable UUID userId) {
        UsersDTO profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UsersDTO> updateProfile(
            @PathVariable UUID userId,
            @RequestBody UsersDTO usersDTO) {
        UsersDTO updatedProfile = userProfileService.updateProfile(userId, usersDTO);
        return ResponseEntity.ok(updatedProfile);
    }
}
