package org.javaup.ai.products.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Integer productId; // 商品ID
    private String productName; // 商品名称
    private Integer categoryId; // 分类ID
    private String mainImageUrl; // 主图URL
    private String otherImageUrls; // 其他图片URL（JSON格式）
    private String description; // 商品描述
    private BigDecimal price; // 当前售价
    private BigDecimal originalPrice; // 原价
    private Integer stockQuantity; // 库存数量
    private Boolean isActive; // 是否上架
    private String purchaseLinks; // 购买链接（JSON格式）
    private String productDetails; // 商品详情（JSON格式）
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
