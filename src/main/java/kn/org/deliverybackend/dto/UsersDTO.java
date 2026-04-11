package kn.org.deliverybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {

    private UUID id;

    private String phoneNumber;

    private Boolean isActive;

    private String firstName;

    private String lastName;

    private String email;

    private String profileImage;

    private Float latitude;

    private Float longitude;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
