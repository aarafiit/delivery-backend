package kn.org.deliverybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionalBannerDTO {

    private UUID id;

    private String imageUrl;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String promotionTitle;

    private String promotionDetails;
}
