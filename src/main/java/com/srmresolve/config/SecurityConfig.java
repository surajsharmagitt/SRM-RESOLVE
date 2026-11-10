package com.srmresolve.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.demo-mode:false}")
    private boolean demoMode;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/error?status=403");
        };
    }

    @Bean
    public org.springframework.security.web.AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getRequestURI();
            if (uri.startsWith("/student")) {
                response.sendRedirect("/student-login");
            } else if (uri.startsWith("/department")) {
                response.sendRedirect("/authority-login");
            } else if (uri.startsWith("/executive")) {
                response.sendRedirect("/admin-login");
            } else {
                response.sendRedirect("/welcome");
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/auth/login";

            for (var authority : authorities) {
                String role = authority.getAuthority();
                if ("ROLE_STUDENT".equals(role)) {
                    redirectUrl = "/student/dashboard";
                    break;
                } else if ("ROLE_DEPT_STAFF".equals(role)) {
                    redirectUrl = "/department/hub";
                    break;
                } else if ("ROLE_DEPT_HEAD".equals(role)) {
                    redirectUrl = "/department/head";
                    break;
                } else if ("ROLE_EXECUTIVE".equals(role) || "ROLE_ADMIN".equals(role)) {
                    redirectUrl = "/executive/dashboard";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/welcome", "/student-login", "/student-login/**", "/authority-login", "/admin-login", "/auth/**", "/demo/**", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error", "/api/enhance-ai", "/api/predict-issue").permitAll()
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/department/head/**").hasAnyRole("DEPT_HEAD", "EXECUTIVE", "ADMIN")
                .requestMatchers("/department/**").hasAnyRole("DEPT_STAFF", "DEPT_HEAD", "EXECUTIVE", "ADMIN")
                .requestMatchers("/executive/**").hasAnyRole("EXECUTIVE", "ADMIN")
                .requestMatchers("/api/simulator/**").hasAnyRole("EXECUTIVE", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login-process")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/welcome")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/student-login/**", "/auth/login-process")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(customAccessDeniedHandler())
                .authenticationEntryPoint(customAuthenticationEntryPoint())
            );

        return http.build();
    }
}
