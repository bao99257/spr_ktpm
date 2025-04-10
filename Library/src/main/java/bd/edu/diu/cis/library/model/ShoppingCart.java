// package bd.edu.diu.cis.library.model;

// import com.nimbusds.jose.shaded.json.annotate.JsonIgnore;
// import lombok.*;

// import javax.persistence.*;
// import java.util.HashSet;
// import java.util.Set;

// @Getter
// @Setter
// @AllArgsConstructor
// @NoArgsConstructor
// @Entity
// @Table(name = "shopping_cart")
// public class ShoppingCart {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     @Column(name = "shopping_cart_id")
//     private Long id;

//     private int totalItems;
//     private double totalPrices;

//     @OneToOne(fetch = FetchType.EAGER)
//     @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
//     // @JsonIgnore
//     private Customer customer;

//     @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart")
//     // @JsonIgnore
//     private Set<CartItem> cartItems = new HashSet<>();
// }





package bd.edu.diu.cis.library.model;

import lombok.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shopping_cart_id")
    private Long id;
    private int totalItems;
    private double totalPrices;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    @JsonIgnore
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart")
    @JsonIgnore
    private Set<CartItem> cartItem;
}
