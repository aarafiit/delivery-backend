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

    private Long id;
    private String name;
    private String address;
    private Float latitude;
    private Float longitude;
    private Long houseNumber;
    private String apartmentName;
    private AddressType addressType;
    private UUID consumerId;
}
