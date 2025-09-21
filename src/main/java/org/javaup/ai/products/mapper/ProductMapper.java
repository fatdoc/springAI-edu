package org.javaup.ai.products.mapper;




import org.apache.ibatis.annotations.Mapper;
import org.javaup.ai.products.domain.Product;

import java.util.List;

@Mapper
public interface ProductMapper {
    void insertProduct(Product product);
    void updateProduct(Product product);
    void deleteProduct(Integer productId);
    Product selectProductById(Integer productId);
    List<Product> selectAllProducts();
    List<Product> selectProductsByCategoryId(Integer categoryId);
    List<Product> selectProductsByName(String productName);
}
