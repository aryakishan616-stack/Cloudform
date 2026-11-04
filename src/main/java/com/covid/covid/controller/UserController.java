package com.covid.covid.controller;

import com.covid.covid.model.User;
import com.covid.covid.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public String csrfToken(CsrfToken csrfToken) {
        return csrfToken.getToken();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody User user,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            user.getUsername().trim().toLowerCase(java.util.Locale.ROOT),
                            user.getPassword()));
            request.getSession(true);
            request.changeSessionId();

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        return userService.signup(user);
    }
}