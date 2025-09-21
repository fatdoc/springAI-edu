package org.javaup.ai.products.service;




import org.javaup.ai.products.domain.Product;
import org.javaup.ai.products.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    public Product createProduct(Product product) {
        productMapper.insertProduct(product);
        return product;
    }

    public Product updateProduct(Product product) {
        productMapper.updateProduct(product);
        return product;
    }

    public void deleteProduct(Integer productId) {
        productMapper.deleteProduct(productId);
    }

    public Product getProductById(Integer productId) {
        return productMapper.selectProductById(productId);
    }

    public List<Product> getAllProducts() {
        return productMapper.selectAllProducts();
    }

    public List<Product> getProductsByCategoryId(Integer categoryId) {
        return productMapper.selectProductsByCategoryId(categoryId);
    }

    public List<Product> getProductsByName(String productName) {
        return productMapper.selectProductsByName(productName);
    }
}