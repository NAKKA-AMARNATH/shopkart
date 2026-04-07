package com.amarnath.shopkart.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category extends BaseEntity{
    @Column(nullable = false)
    String name;

    @Column(unique = true, nullable = false)
    String slug;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "is_active")
    boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    List<Category> children = new ArrayList<>();
}

