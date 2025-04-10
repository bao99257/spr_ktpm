package bd.edu.diu.cis.customer.config;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import bd.edu.diu.cis.customer.api.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class CustomerConfiguration  {

    @Bean
    public UserDetailsService userDetailsService(){
        return new CustomerServiceConfig();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider provider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //     auth.authenticationProvider(provider());
    // }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .antMatcher("/api/**") // Chỉ áp dụng cho /api/**
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .authorizeRequests(auth -> auth
                .antMatchers("/api/register", "/api/generateToken", "/api/welcome").permitAll()
                .antMatchers("/api/user/**").hasAuthority("CUSTOMER")
                .antMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    protected SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests(auth -> auth
                .antMatchers("/*").permitAll()
                .antMatchers("/customer/*").hasAuthority("CUSTOMER")
        )
                .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/do-login")
                .defaultSuccessUrl("/")
                .permitAll()
                )
                
                .logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );
                return http.build();
    }

    // @Bean
    // @Order(2)
    // protected void configure(HttpSecurity http) throws Exception {
    //     http.authorizeRequests()
    //             .antMatchers("/*").permitAll()
    //             .antMatchers("/customer/*").hasAuthority("CUSTOMER")
    //             .and()
    //             .formLogin()
    //             .loginPage("/login")
    //             .loginProcessingUrl("/do-login")
    //             .defaultSuccessUrl("/")
    //             .and()
    //             .logout()
    //             .invalidateHttpSession(true)
    //             .clearAuthentication(true)
    //             .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
    //             .logoutSuccessUrl("/login?logout")
    //             .permitAll();
    // }
}
