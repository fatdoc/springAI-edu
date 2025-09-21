package org.javaup.ai.products.mapper;




import org.apache.ibatis.annotations.Mapper;
import org.javaup.ai.products.domain.Category;

import java.util.List;

@Mapper
public interface CategoryMapper {
    void insertCategory(Category category);
    void updateCategory(Category category);
    void deleteCategory(Integer categoryId);
    Category selectCategoryById(Integer categoryId);
    List<Category> selectAllCategories();
}