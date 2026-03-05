package kn.org.deliverybackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kn.org.deliverybackend.enumeration.AddressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for address information")
public class AddressesDTO {
    @Schema(description = "Address ID", example = "1")
    private Long id;
    
    @Schema(description = "Address name (e.g., Home, Work)", example = "Home")
    private String name;
    
    @Schema(description = "Full address string", example = "123 Main Street, Dhaka 1200")
    private String address;
    
    @Schema(description = "Latitude coordinate", example = "23.7104")
    private Float latitude;
    
    @Schema(description = "Longitude coordinate", example = "90.4074")
    private Float longitude;
    
    @Schema(description = "House number", example = "123")
    private Long houseNumber;
    
    @Schema(description = "Apartment/building name", example = "Dhaka Tower, Block A")
    private String apartmentName;
    
    @Schema(description = "Type of address", example = "HOME")
    private AddressType addressType;
    
    @Schema(description = "Consumer/User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID consumerId;
}
