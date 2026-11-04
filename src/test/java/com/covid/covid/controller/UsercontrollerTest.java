package com.covid.covid.controller;

import com.covid.covid.model.User;
import com.covid.covid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsercontrollerTest {
    private final UserService userService = mock(UserService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final SecurityContextRepository securityContextRepository =
            mock(SecurityContextRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
            new UserController(userService, authenticationManager, securityContextRepository)
    ).build();

    @BeforeEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void loginAuthenticatesAndStoresSessionContext() throws Exception {
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                "person@example.com", null, java.util.List.of());
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authenticated);

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"person@example.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(securityContextRepository).saveContext(
                any(org.springframework.security.core.context.SecurityContext.class),
                any(), any());
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("invalid"));

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"person@example.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));

        verify(securityContextRepository, org.mockito.Mockito.never())
                .saveContext(any(), any(), any());
    }

    @Test
    void signupPassesCredentialsToService() throws Exception {
        when(userService.signup(any(User.class))).thenReturn("Signup successful");

        mockMvc.perform(post("/api/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"person@example.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Signup successful"));

        verify(userService).signup(any(User.class));
    }
}
