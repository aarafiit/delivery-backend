package kn.org.deliverybackend.service.impl;

import kn.org.deliverybackend.dto.request.product.ProductRequestDTO;
import kn.org.deliverybackend.dto.response.product.ProductResponseDTO;
import kn.org.deliverybackend.entity.Inventory;
import kn.org.deliverybackend.entity.Product;

import kn.org.deliverybackend.mapper.ProductMapper;
import kn.org.deliverybackend.repository.CategoryRepository;
import kn.org.deliverybackend.repository.InventoryRepository;
import kn.org.deliverybackend.repository.ProductRepository;
import kn.org.deliverybackend.service.FileStorageService;
import kn.org.deliverybackend.service.InventoryService;
import kn.org.deliverybackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductResponseDTO> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toEnrichedResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toEnrichedResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toEnrichedResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDTO> getProductsPaged(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toEnrichedResponseDTO);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toEnrichedResponseDTO)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile[] images) {
        Product product = productMapper.toEntity(productRequestDTO);

        List<String> uploaded = uploadImages(images);
        if (!uploaded.isEmpty()) {
            product.setImageUrls(uploaded);
        }
        // Primary image mirrors the first gallery image when not explicitly set
        applyPrimaryImage(product);

        calculateAndSetDiscountPrice(product, productRequestDTO);
        if (productRequestDTO.getLowStockThreshold() != null) {
            product.setLowStockThreshold(productRequestDTO.getLowStockThreshold());
        }
        Product saved = productRepository.save(product);

        // Auto-create inventory row for the new product (starts at 0 stock)
        Inventory inventory = new Inventory();
        inventory.setProductId(saved.getId());
        inventory.setStockQuantity(0);
        inventory.setUnit(productRequestDTO.getUnit());
        inventoryRepository.save(inventory);

        return toEnrichedResponseDTO(saved);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO, MultipartFile[] images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // If new files are uploaded, they replace the existing gallery.
        // Otherwise, if the request carries an explicit URL list, use that.
        List<String> uploaded = uploadImages(images);
        if (!uploaded.isEmpty()) {
            product.setImageUrls(uploaded);
            product.setImageUrl(uploaded.get(0));
        } else if (productRequestDTO.getImageUrls() != null) {
            product.setImageUrls(new ArrayList<>(productRequestDTO.getImageUrls()));
        }

        product.setCategoryId(productRequestDTO.getCategoryId());
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        calculateAndSetDiscountPrice(product, productRequestDTO);
        product.setShopId(productRequestDTO.getShopId());
        if (productRequestDTO.getImageUrl() != null && !productRequestDTO.getImageUrl().isBlank()) {
            product.setImageUrl(productRequestDTO.getImageUrl());
        }
        // Keep primary image consistent with the gallery
        applyPrimaryImage(product);
        product.setIsAvailable(productRequestDTO.getIsAvailable());
        if (productRequestDTO.getUnit() != null) product.setUnit(productRequestDTO.getUnit());
        if (productRequestDTO.getLowStockThreshold() != null) {
            product.setLowStockThreshold(productRequestDTO.getLowStockThreshold());
        }

        return toEnrichedResponseDTO(productRepository.save(product));
    }

    private ProductResponseDTO toEnrichedResponseDTO(Product product) {
        ProductResponseDTO dto = productMapper.toResponseDTO(product);
        dto.setStockStatus(inventoryService.computeStatus(product));
        dto.setSku(product.getSku());
        dto.setUnit(product.getUnit());
        // Enrich with category name
        if (product.getCategoryId() != null) {
            categoryRepository.findById(product.getCategoryId())
                    .ifPresent(cat -> dto.setCategoryName(cat.getName()));
        }
        return dto;
    }

    /** Uploads every non-empty file and returns the resulting URLs in order. */
    private List<String> uploadImages(MultipartFile[] images) {
        List<String> urls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    urls.add(fileStorageService.uploadFile(image));
                }
            }
        }
        return urls;
    }

    /** Ensures the primary imageUrl points at the first gallery image when not set. */
    private void applyPrimaryImage(Product product) {
        if ((product.getImageUrl() == null || product.getImageUrl().isBlank())
                && product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            product.setImageUrl(product.getImageUrls().get(0));
        }
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
