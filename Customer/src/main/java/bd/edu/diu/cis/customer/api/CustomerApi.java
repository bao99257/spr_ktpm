package bd.edu.diu.cis.customer.api;

import bd.edu.diu.cis.library.dto.CategoryDto;
import bd.edu.diu.cis.library.dto.CustomerDto;
import bd.edu.diu.cis.library.dto.ProductDto;
import bd.edu.diu.cis.library.model.Customer;
import bd.edu.diu.cis.library.model.Order;
import bd.edu.diu.cis.library.model.Product;
import bd.edu.diu.cis.library.model.ShoppingCart;
import bd.edu.diu.cis.library.repository.OrderRepository;
import bd.edu.diu.cis.library.service.CategoryService;
import bd.edu.diu.cis.library.service.CustomerService;
import bd.edu.diu.cis.library.service.OrderService;
import bd.edu.diu.cis.library.service.ProductService;
import bd.edu.diu.cis.library.service.ShoppingCartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import bd.edu.diu.cis.library.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CustomerApi {

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    // Đăng kí
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> processRegister(
            @Valid @RequestBody CustomerDto customerDto,
            BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (result.hasErrors()) {
                response.put("errors", result.getAllErrors());
                response.put("customerDto", customerDto);
                return ResponseEntity.badRequest().body(response);
            }

            Customer existingCustomer = customerService.findByUsername(customerDto.getUsername());
            if (existingCustomer != null) {
                response.put("error", "Username has been registered");
                response.put("customerDto", customerDto);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            if (!customerDto.getPassword().equals(customerDto.getRepeatPassword())) {
                response.put("error", "Passwords do not match");
                response.put("customerDto", customerDto);
                return ResponseEntity.badRequest().body(response);
            }

            customerDto.setPassword(passwordEncoder.encode(customerDto.getPassword()));
            customerService.save(customerDto);

            response.put("message", "Registration successful");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("error", "Server encountered some problems");
            response.put("customerDto", customerDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Lấy Token
    @PostMapping("/generateToken")
    public ResponseEntity<String> generateToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return ResponseEntity.ok(jwtService.generateToken(authRequest.getUsername()));
        } else {
            throw new UsernameNotFoundException("Invalid credentials");
        }
    }

    // Vào home
    // @GetMapping("/user/home")
    // public ResponseEntity<Map<String, Object>> index() {
    // Map<String, Object> response = new HashMap<>();

    // // Lấy thông tin người dùng từ SecurityContextHolder (sau khi token được xác
    // // thực)
    // String username = null;
    // Object principal =
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // if (principal instanceof UserDetails) {
    // username = ((UserDetails) principal).getUsername();
    // } else {
    // username = principal.toString();
    // }

    // // Nếu không có thông tin người dùng (token không hợp lệ), trả về lỗi
    // if (username == null || username.equals("anonymousUser")) {
    // response.put("error", "Unauthorized access. Please provide a valid token.");
    // return ResponseEntity.status(401).body(response);
    // }

    // // Lấy thông tin customer và shopping cart
    // Customer customer = customerService.findByUsername(username);
    // ShoppingCart cart = customer.getShoppingCart();

    // if (cart == null) {
    // response.put("totalItems", "0");
    // } else {
    // response.put("totalItems", cart.getTotalItems());
    // }
    // response.put("username", username);

    // // Lấy dữ liệu danh mục và sản phẩm
    // List<Category> categories = categoryService.findAll();
    // List<ProductDto> productDtos = productService.findAll();
    // List<CategoryDto> categoryDtoList = categoryService.getCategoryAndProduct();

    // // Thêm dữ liệu vào response
    // // response.put("categoriesSeperate", categoryDtoList);
    // // response.put("categories", categories);
    // response.put("products", productDtos);
    // response.put("title", "JGPS - Jona General Purpose Shop");

    // return ResponseEntity.ok(response);
    // }

    @GetMapping("/user/home")
    public ResponseEntity<Map<String, Object>> getHomeData() {
        try {
            List<Product> products = productService.getAllProducts(); // Giả sử phương thức này trả về danh sách Product

            // Chuyển danh sách Product thành danh sách ProductDto
            List<ProductDto> productDtos = products.stream()
                    .map(ProductDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("title", "JGPS - Jona General Purpose");
            response.put("products", productDtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("error", "Failed to load home data");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Lấy thông tin account
    @GetMapping("/user/account")
    public ResponseEntity<Map<String, Object>> accountHome() {
        Map<String, Object> response = new HashMap<>();

        // Lấy thông tin người dùng từ SecurityContextHolder (sau khi token được xác
        // thực)
        String username = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Nếu không có thông tin người dùng (token không hợp lệ), trả về lỗi
        if (username == null || username.equals("anonymousUser")) {
            response.put("error", "Unauthorized access. Please provide a valid token.");
            return ResponseEntity.status(401).body(response);
        }

        // Lấy thông tin customer
        Customer customer = customerService.findByUsername(username);
        if (customer == null) {
            response.put("error", "Customer not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Thêm thông tin vào response
        response.put("title", customer.getFirstName() + " account");
        response.put("customer", customer);

        return ResponseEntity.ok(response);
    }

    // Tìm sản phẩm theo id
    // Tìm sản phẩm theo id
    // @GetMapping("/user/find-product/{id}")
    // public ResponseEntity<Map<String, Object>>
    // findProductByIdApi(@PathVariable("id") long id) {
    // try {
    // Product product = productService.getProductById(id);
    // if (product == null || product.getCategory() == null) {
    // throw new RuntimeException("Product or category not found");
    // }

    // Long categoryId = product.getCategory().getId();
    // List<Product> products = productService.getRelatedProducts(categoryId);

    // Map<String, Object> response = new HashMap<>();
    // response.put("product", product);
    // response.put("relatedProducts", products);
    // response.put("title", product.getName());

    // return ResponseEntity.ok(response);
    // } catch (Exception e) {
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("status", "error");
    // errorResponse.put("error", "Product not found");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("timestamp", LocalDateTime.now());
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    // }
    // }

    @GetMapping("/user/find-product/{id}")
    public ResponseEntity<Map<String, Object>> findProductByIdApi(@PathVariable("id") long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null || product.getCategory() == null) {
                throw new RuntimeException("Product or category not found");
            }

            Long categoryId = product.getCategory().getId();
            List<Product> relatedProducts = productService.getRelatedProducts(categoryId);

            // Chuyển Product thành ProductDto
            ProductDto productDto = new ProductDto(product);
            List<ProductDto> relatedProductDtos = relatedProducts.stream()
                    .map(ProductDto::new)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("product", productDto);
            response.put("relatedProducts", relatedProductDtos);
            response.put("title", product.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("error", "Product not found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Xem cart
    @GetMapping("/user/cart")
    public ResponseEntity<Map<String, Object>> getCartDetails(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("status", "error");
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);
            ShoppingCart shoppingCart = customer.getShoppingCart();

            if (shoppingCart == null || shoppingCart.getCartItem().isEmpty()) {
                response.put("status", "success");
                response.put("message", "No items in your cart");
                response.put("cartItems", Collections.emptyList());
                return ResponseEntity.ok(response);
            }

            // Tạo response chi tiết
            response.put("status", "success");
            response.put("totalItems", shoppingCart.getTotalItems());
            response.put("subTotal", shoppingCart.getTotalPrices());
            response.put("discountPrice", customerService.calculateDiscount(shoppingCart.getTotalPrices()));

            // Chuyển đổi cart items sang DTO nếu cần
            List<Map<String, Object>> cartItems = shoppingCart.getCartItem().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("productId", item.getProduct().getId());
                        itemMap.put("productName", item.getProduct().getName());
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("unitPrice", item.getProduct().getSalePrice());
                        itemMap.put("totalPrice", item.getTotalPrice());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            response.put("cartItems", cartItems);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get cart details: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Add to cart
    @PostMapping("/user/add-item")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @RequestBody Map<String, Object> requestBody, // Thay đổi từ @RequestParam sang @RequestBody
            Principal principal) {

        // Lấy tham số từ JSON
        Long productId = Long.valueOf(requestBody.get("productId").toString());
        int quantity = requestBody.containsKey("quantity") ? Integer.parseInt(requestBody.get("quantity").toString())
                : 1;

        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("status", "error");
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Product product = productService.getProductById(productId);
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);

            ShoppingCart cart = cartService.addItemToCart(product, quantity, customer);

            response.put("status", "success");
            response.put("message", "Product added to cart successfully");
            response.put("cartId", cart.getId());
            response.put("totalItems", cart.getTotalItems());
            response.put("totalPrice", cart.getTotalPrices());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to add item to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Post Order
    @PostMapping("/user/complete-order")
    public ResponseEntity<Map<String, Object>> completeOrder(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra đăng nhập
        if (principal == null) {
            response.put("status", "error");
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);
            ShoppingCart cart = customer.getShoppingCart();

            // Kiểm tra giỏ hàng
            if (cart == null || cart.getCartItem().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Cart is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Bước 1: Lưu đơn hàng (không cần gán kết quả)
            orderService.saveOrder(cart);

            // Bước 2: Lấy đơn hàng mới nhất của khách hàng
            Order order = orderRepository.findTopByCustomerOrderByOrderDateDesc(customer);

            if (order == null) {
                throw new RuntimeException("Failed to retrieve saved order");
            }

            // Trả về response
            response.put("status", "success");
            response.put("message", "Order completed successfully");
            response.put("orderId", order.getId());
            response.put("orderDate", order.getOrderDate());
            response.put("totalAmount", order.getTotalPrice());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to complete order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xem Order
    @GetMapping("/user/orders")
    public ResponseEntity<Map<String, Object>> getUserOrders(Principal principal) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra đăng nhập
        if (principal == null) {
            response.put("status", "error");
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String username = principal.getName();
            Customer customer = customerService.findByUsername(username);
            List<Order> orderList = customer.getOrders();

            // Chuẩn bị dữ liệu orders để trả về
            List<Map<String, Object>> ordersData = orderList.stream()
                    .map(order -> {
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("orderId", order.getId());
                        orderData.put("orderDate", order.getOrderDate());
                        orderData.put("totalPrice", order.getTotalPrice());
                        orderData.put("status", order.getOrderStatus());
                        // Thêm các thông tin khác nếu cần
                        return orderData;
                    })
                    .collect(Collectors.toList());

            // Xây dựng response
            response.put("status", "success");
            response.put("title", customer.getFirstName() + " orders");
            response.put("customer", Map.of(
                    "firstName", customer.getFirstName(),
                    "lastName", customer.getLastName(),
                    "email", customer.getUsername()
            // Thêm các thông tin khác nếu cần
            ));
            response.put("orders", ordersData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to get orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    static class AuthRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
