package kn.org.deliverybackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for home page data")
public class HomePageResponseDTO {
    @Schema(description = "List of promotional banners")
    private List<PromotionalBannerDTO> banners;
    
    @Schema(description = "List of categories")
    private List<CategoryDTO> categories;
    
    @Schema(description = "List of popular items")
    private List<ProductDTO> popularItems;
    
    @Schema(description = "List of featured items")
    private List<ProductDTO> featuredItems;
}
