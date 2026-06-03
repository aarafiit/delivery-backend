package kn.org.deliverybackend.dto.response.search;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSearchResponse {
    private String query;
    private SearchGroup products;
    private SearchGroup categories;
    private SearchGroup orders;
    private SearchGroup customers;
    private SearchGroup riders;
    private SearchGroup reviews;
    private long totalCount;
}
//ProductRepository.java — added searchAdmin and searchCustomer
//CategoryRepository.java — added searchAdmin and searchCustomer
//OrderRepository.java — added searchAdmin
//UsersRepository.java — added searchAdmin
//UserRiderRepository.java — added searchAdmin
//ReviewRepository.java — added searchAdmin
//ShopRepository.java — added search
//PromotionalBannerRepository.java — added searchAdmin and searchCustomerActive
//SecurityConfig.java — added /api/search/** to permitAll