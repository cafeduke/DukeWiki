package com.github.cafeduke.dukewiki.config;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${dukewiki.usernames}")
    private String usernames;
    
    @Value("${dukewiki.passwords}")
    private String passwords;
    
    @Value("${dukewiki.roles}")
    private String roles;
    
    public static void main (String arg[])
    {
        System.out.println(Arrays.asList("aafd.asfa|bafas|cafasdf".split("\\|")));
    }
    
    /**
     * Configure users with their password hash and assign them to roles. 
     * @return UserDetailsService object having collection of users.
     */
    @Bean
    public UserDetailsService userDetailsService ()
    {
        List<UserDetails> listUserDetail = new ArrayList<> ();
        
        String user[] = usernames.split("\\|");
        String passwd[] = passwords.split("\\|");
        String role[]= roles.split("\\|");
        
        Validate.isTrue (user.length == passwd.length && user.length == role.length, 
            "User, password and role count are not the same. Note: fields are separated by '|'. user.length=%d pass.length=%d role.length=%d", 
            user.length, passwd.length, role.length);
        
        for (int i = 0; i < user.length; ++i)
            listUserDetail.add(User.withUsername(StringUtils.trim(user[i]))
                .password(StringUtils.trim(passwd[i]))
                .roles(role[i].split(" *,"))
                .build());
        
        return new InMemoryUserDetailsManager(listUserDetail);
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
