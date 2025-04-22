package bd.edu.diu.cis.admin.restapi;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bd.edu.diu.cis.library.dto.AdminDto;
import bd.edu.diu.cis.library.dto.CustomerDto;
import bd.edu.diu.cis.library.model.Admin;
import bd.edu.diu.cis.library.model.Customer;
import bd.edu.diu.cis.library.repository.AdminRepository;
import bd.edu.diu.cis.library.service.impl.AdminServiceImpl;
import bd.edu.diu.cis.library.service.impl.CustomerServiceImpl;

@RestController
@RequestMapping("/api")
public class AdminApi {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerServiceImpl customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AdminServiceImpl adminService;

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Welcome to the API!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> addNewAdmin(@Valid @RequestBody AdminDto adminDto, BindingResult result) {
        try {
            // Kiểm tra validation errors
            if (result.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(errors);
            }

            // Kiểm tra username đã tồn tại
            String username = adminDto.getUsername();
            Admin admin = adminService.findByUsername(username);
            if (admin != null) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("emailError", "Your email has been registered!"));
            }

            // Kiểm tra password matching
            if (!adminDto.getPassword().equals(adminDto.getRepeatPassword())) {
                return ResponseEntity.badRequest().body(
                        Collections.singletonMap("passwordError", "Your password maybe wrong! Check again!"));
            }

            // Mã hóa password và lưu
            adminDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
            adminService.save(adminDto);

            return ResponseEntity.ok(
                    Collections.singletonMap("success", "Register successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Collections.singletonMap("errors", "The server has been wrong!"));
        }
    }

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

    @GetMapping("/admin/customers")
    public ResponseEntity<?> getCustomers(Principal principal) {
        // Kiểm tra xác thực
        if (principal == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized access. Please log in.");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Lấy danh sách khách hàng và chuyển đổi sang DTO (chỉ lấy firstName và
        // lastName)
        List<Customer> customers = customerService.listAll();
        List<CustomerDto> customerDTOs = customers.stream()
                .map(c -> {
                    CustomerDto dto = new CustomerDto();
                    dto.setFirstName(c.getFirstName());
                    dto.setLastName(c.getLastName());
                    dto.setUsername(c.getUsername());
                    dto.setPassword(c.getPassword());
                    dto.setRepeatPassword(c.getPassword());
                    return dto;
                })
                .collect(Collectors.toList());

        // Tạo response
        Map<String, Object> response = new HashMap<>();
        response.put("customers", customerDTOs);
        response.put("size", customerDTOs.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
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