package kn.org.deliverybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private Long id;

    private String userName;

    private String password;

    private String role;
}
