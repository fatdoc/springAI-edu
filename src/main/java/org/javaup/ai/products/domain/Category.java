package org.javaup.ai.products.domain;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

@Data
public class Category {
    private Integer categoryId; // 分类ID
    private String categoryName; // 分类名称
    private Integer parentCategoryId; // 父分类ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}

