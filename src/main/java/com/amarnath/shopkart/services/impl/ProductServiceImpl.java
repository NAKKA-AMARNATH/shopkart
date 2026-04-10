package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.config.CacheConfig;
import com.amarnath.shopkart.dto.request.CreateProductRequest;
import com.amarnath.shopkart.dto.request.ProductVariantRequest;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.ProductResponse;
import com.amarnath.shopkart.dto.response.ProductVariantResponse;
import com.amarnath.shopkart.entities.*;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.*;
import com.amarnath.shopkart.services.interfaces.ProductService;
import com.amarnath.shopkart.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    CategoryRepository categoryRepository;
    UserRepository userRepository;
    ReviewRepository reviewRepository;

    // ── CREATE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_LIST, allEntries = true),
    })
    public ProductResponse createProduct(CreateProductRequest request, UUID sellerId) {
        String slug = SlugUtils.generateSlug(request.getName());
        if (productRepository.existsBySlug(slug)) {
            throw new BusinessException("Product with this name already exists");
        }

        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("Product with this SKU already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller not found with id: " + sellerId));

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .brand(request.getBrand())
                .sku(request.getSku())
                .category(category)
                .seller(seller)
                .isActive(true)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        Product savedProduct = productRepository.save(product);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (ProductVariantRequest variantRequest : request.getVariants()) {
                if (variantRequest.getSku() != null &&
                        productVariantRepository.existsBySku(variantRequest.getSku())) {
                    throw new BusinessException(
                            "Variant with SKU " + variantRequest.getSku() + " already exists");
                }
                ProductVariant variant = ProductVariant.builder()
                        .product(savedProduct)
                        .color(variantRequest.getColor())
                        .size(variantRequest.getSize())
                        .price(variantRequest.getPrice())
                        .stockQuantity(variantRequest.getStockQuantity() != null
                                ? variantRequest.getStockQuantity() : 0)
                        .imageUrl(variantRequest.getImageUrl())
                        .sku(variantRequest.getSku())
                        .build();
                savedProduct.getVariants().add(productVariantRepository.save(variant));
            }
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .sortOrder(i)
                        .build();
                savedProduct.getImages().add(image);
            }
            productRepository.save(savedProduct);
        }

        return mapToResponse(savedProduct);
    }

    // ── READ ───────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#id")
    public ProductResponse getProductById(UUID id) {
        return mapToResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "'slug:' + #slug")
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT_LIST, key = "'all:p' + #page + ':s' + #size")
    public PagedResponse<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByIsActiveTrue(pageable);
        return mapToPagedResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_PRODUCT_LIST,
            key = "'cat:' + #categoryId + ':p' + #page + ':s' + #size")
    public PagedResponse<ProductResponse> getProductsByCategory(
            UUID categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository
                .findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return mapToPagedResponse(productPage);
    }

    // ── SEARCH (not cached — too many filter combinations) ─────────────────────
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(
            UUID categoryId, String brand, BigDecimal minPrice,
            BigDecimal maxPrice, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findWithFilters(
                categoryId, brand, minPrice, maxPrice, search, pageable);
        return mapToPagedResponse(productPage);
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_LIST, allEntries = true),
    })
    public ProductResponse updateProduct(UUID id, CreateProductRequest request, UUID sellerId) {
        Product product = findProductById(id);

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("You are not authorized to update this product");
        }

        String newSlug = SlugUtils.generateSlug(request.getName());
        if (!newSlug.equals(product.getSlug()) && productRepository.existsBySlug(newSlug)) {
            throw new BusinessException("Product with this name already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setSlug(newSlug);
        product.setDescription(request.getDescription());
        product.setBasePrice(request.getBasePrice());
        product.setBrand(request.getBrand());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    // ── DELETE (soft delete — still needs eviction) ────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCTS,     allEntries = true),
            @CacheEvict(value = CacheConfig.CACHE_PRODUCT_LIST, allEntries = true),
    })
    public void deleteProduct(UUID id, UUID sellerId) {
        Product product = findProductById(id);

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException("You are not authorized to delete this product");
        }

        product.setActive(false);
        productRepository.save(product);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────
    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    private ProductResponse mapToResponse(Product product) {
        List<ProductVariantResponse> variantResponses = product.getVariants()
                .stream()
                .map(v -> ProductVariantResponse.builder()
                        .id(v.getId())
                        .color(v.getColor())
                        .size(v.getSize())
                        .price(v.getPrice())
                        .stockQuantity(v.getStockQuantity())
                        .imageUrl(v.getImageUrl())
                        .sku(v.getSku())
                        .build())
                .toList();

        List<String> imageUrls = product.getImages()
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .brand(product.getBrand())
                .sku(product.getSku())
                .isActive(product.isActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller().getId())
                .sellerName(product.getSeller().getFirstName()
                        + " " + product.getSeller().getLastName())
                .variants(variantResponses)
                .imageUrls(imageUrls)
                .averageRating(avgRating)
                .createdAt(product.getCreatedAt())
                .build();
    }

    private PagedResponse<ProductResponse> mapToPagedResponse(Page<Product> page) {
        return PagedResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}