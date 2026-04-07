package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParentId(UUID parentId);

    List<Category> findByIsActiveTrue();

}

