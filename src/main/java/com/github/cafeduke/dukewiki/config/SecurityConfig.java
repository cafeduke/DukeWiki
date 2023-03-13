package com.github.cafeduke.dukewiki.config;

import java.util.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class shall provide create beans required for authentication and authorization 
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig
{
    /**
     * Configure users with their password hash and assign them to roles. 
     * @return UserDetailsService object having collection of users.
     */
    @Bean
    public UserDetailsService userDetailsService ()
    {
        List<UserDetails> listUser = new ArrayList<> ();
        
        // Otd007#1
        listUser.add(User.withUsername("rbseshad")
            .password("{bcrypt}$2a$10$.7ef1.xsC0yCzBrErY5GQetkkP8vU4xn8MQUd/7zXY.tv2uAvG2l6")
            .roles("ADMIN", "READER")
            .build());
        
        listUser.add(User.withUsername("prevenka")
            .password("{bcrypt}$2a$10$.7ef1.xsC0yCzBrErY5GQetkkP8vU4xn8MQUd/7zXY.tv2uAvG2l6")
            .roles("ADMIN", "READER")
            .build());

        // welcome1
        listUser.add(User.withUsername("oracle")
            .password("{bcrypt}$2a$10$ib.9mff763hHkx..6/lcBOQgoZVgSZvGuoqlDC4h3CBfiLTFc1DGO")
            .roles("READER")
            .build());
        
        return new InMemoryUserDetailsManager(listUser);
    }    
    
    @Bean
    public SecurityFilterChain securityChainFilter (HttpSecurity http) throws Exception
    {
        http.formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/home", true)
            .permitAll();
        
        http.logout()
            .permitAll();
    
        http.authorizeHttpRequests()
            .requestMatchers("/css/**", "/js/**", "/images/**")
            .permitAll();
        
        http.authorizeHttpRequests()
            .anyRequest()
            .hasRole("READER");
            
        return http.build();
    }
     
}
