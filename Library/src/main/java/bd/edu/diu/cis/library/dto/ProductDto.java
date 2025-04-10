package bd.edu.diu.cis.library.dto;

import bd.edu.diu.cis.library.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private double costPrice;
    private double salePrice;
    private int currentQuantity;
    private CategoryDto category;
    private String image; // Chuỗi Base64
    private boolean activated;
    private boolean deleted;

    // Constructor để chuyển từ Product entity sang ProductDto
    public ProductDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.costPrice = product.getCostPrice();
        this.salePrice = product.getSalePrice();
        this.currentQuantity = product.getCurrentQuantity();
        this.category = product.getCategory() != null ? new CategoryDto(
            product.getCategory().getId(),
            product.getCategory().getName(),
            product.getCategory().getDescription(),
            null // numberOfProduct: để null vì không có dữ liệu trực tiếp từ Category
        ) : null;
        this.image = product.getImage(); // Không cần encode vì đã là chuỗi Base64
        this.activated = product.is_activated();
        this.deleted = product.is_deleted();
    }
}



// package bd.edu.diu.cis.library.dto;

// import bd.edu.diu.cis.library.model.Category;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class ProductDto {
//     private Long id;
//     private String name;
//     private String description;
//     private double costPrice;
//     private double salePrice;
//     private int currentQuantity;
//     private Category category;
//     private String image;
//     private boolean activated;
//     private boolean deleted;
// }
