package kn.org.deliverybackend.mapper;

import kn.org.deliverybackend.dto.PromotionalBannerDTO;
import kn.org.deliverybackend.entity.PromotionalBanner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionalBannerMapper {

    PromotionalBannerDTO toDTO(PromotionalBanner promotionalBanner);

    PromotionalBanner toEntity(PromotionalBannerDTO promotionalBannerDTO);
}
