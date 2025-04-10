package bd.edu.diu.cis.library.model;


import lombok.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_items") // Sửa tên bảng thành "cart_items" cho phù hợp
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id") // Sửa tên cột thành "cart_item_id" cho phù hợp
    private Long id;

    private int quantity;
    private double totalPrice;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "shopping_cart_id", referencedColumnName = "shopping_cart_id")
    @JsonIgnore
    private ShoppingCart cart;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    @JsonIgnore
    private Product product;
}





// package bd.edu.diu.cis.library.model;

// import lombok.*;

// import javax.persistence.*;

// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Entity
// @Table(name = "cart_item")
// public class CartItem {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     @Column(name = "order_detail_id")
//     private Long id;
//     private int quantity;
//     private double totalPrice;
//     @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//     @JoinColumn(name = "shopping_cart_id", referencedColumnName = "shopping_cart_id")
//     private ShoppingCart cart;


//     @OneToOne(fetch = FetchType.EAGER)
//     @JoinColumn(name = "product_id", referencedColumnName = "product_id")
//     private Product product;
// }
