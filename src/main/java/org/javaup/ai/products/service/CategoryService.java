package org.javaup.ai.products.service;




import org.javaup.ai.products.domain.Category;
import org.javaup.ai.products.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public Category createCategory(Category category) {
        categoryMapper.insertCategory(category);
        return category;
    }

    public Category updateCategory(Category category) {
        categoryMapper.updateCategory(category);
        return category;
    }

    public void deleteCategory(Integer categoryId) {
        categoryMapper.deleteCategory(categoryId);
    }

    public Category getCategoryById(Integer categoryId) {
        return categoryMapper.selectCategoryById(categoryId);
    }

    public List<Category> getAllCategories() {
        return categoryMapper.selectAllCategories();
    }
}