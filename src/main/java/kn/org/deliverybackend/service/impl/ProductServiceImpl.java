package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.request.product.ProductRequestDTO;
import kn.org.deliverybackend.dto.response.product.ProductResponseDTO;
import kn.org.deliverybackend.entity.Product;
import kn.org.deliverybackend.mapper.ProductMapper;
import kn.org.deliverybackend.repository.ProductRepository;
import kn.org.deliverybackend.service.FileStorageService;
import kn.org.deliverybackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Override
    public List<ProductResponseDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        // Using native query from ProductRepository
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile image) {
        Product product = productMapper.toEntity(productRequestDTO);
        
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(image);
            product.setImageUrl(imageUrl);
        }
        
        calculateAndSetDiscountPrice(product, productRequestDTO);
        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(image);
            product.setImageUrl(imageUrl);
        }
        
        product.setCategoryId(productRequestDTO.getCategoryId());
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        calculateAndSetDiscountPrice(product, productRequestDTO);
        product.setShopId(productRequestDTO.getShopId());
        // Only update imageUrl if provided, otherwise keep existing
        if (productRequestDTO.getImageUrl() != null && !productRequestDTO.getImageUrl().isBlank()) {
            product.setImageUrl(productRequestDTO.getImageUrl());
        }
        product.setIsAvailable(productRequestDTO.getIsAvailable());
        
        return productMapper.toResponseDTO(productRepository.save(product));
    }

    private void calculateAndSetDiscountPrice(Product product, ProductRequestDTO dto) {
        if (dto.getDiscountPercentage() != null && dto.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = dto.getPrice()
                    .multiply(dto.getDiscountPercentage())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            product.setDiscountPrice(dto.getPrice().subtract(discountAmount));
        } else {
            product.setDiscountPrice(dto.getPrice());
        }
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
