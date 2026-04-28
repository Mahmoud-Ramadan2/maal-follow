package com.mahmoud.maalflow.security;

import com.mahmoud.maalflow.security.AuthController;
import com.mahmoud.maalflow.modules.shared.user.controller.UserController;
import com.mahmoud.maalflow.modules.shared.user.dto.UserSummary;
import com.mahmoud.maalflow.modules.shared.user.service.UserService;
import com.mahmoud.maalflow.security.dto.AuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {AuthController.class, UserController.class})
class SecurityAuthorizationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_isPublic() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .build();

        when(authenticationService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"admin@example.com",
                                  "password":"secret123"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_isPublic() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .tokenType("Bearer")
                .build();

        when(authenticationService.refreshToken(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken":"token-value"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void apiRequiresAuthentication_whenNoTokenOrSession() throws Exception {
        mockMvc.perform(get("/api/v1/contracts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void usersEndpoint_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void usersEndpoint_allowedForAdmin() throws Exception {
        when(userService.list(anyInt(), anyInt(), nullable(String.class), any()))
                .thenReturn(new PageImpl<>(List.of(UserSummary.builder().id(1L).name("Admin").build())));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    void unknownNonApiRoute_deniedByDefault() throws Exception {
        mockMvc.perform(get("/internal/ping"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        OncePerRequestFilter testAuthorizationFilter() {
            return new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    String path = request.getRequestURI();

                    if (path.startsWith("/api/v1/auth/")) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    if (!path.startsWith("/api/v1/")) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }

                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}




