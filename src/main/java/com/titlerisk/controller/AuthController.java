package com.titlerisk.controller;

import com.titlerisk.dto.AuthResponse;
import com.titlerisk.dto.LoginRequest;
import com.titlerisk.dto.RegisterRequest;
import com.titlerisk.model.User;
import com.titlerisk.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registration, login, logout, and "who am I" — session-cookie based, no
 * tokens to manage on the frontend. A successful login or registration
 * leaves the browser holding a session cookie that every subsequent
 * {@code fetch()} call sends automatically (same-origin requests always
 * include cookies), so the rest of the frontend never has to think about it.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password();

        if (username.length() < MIN_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username must be at least " + MIN_USERNAME_LENGTH + " characters.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already taken.");
        }

        userRepository.save(new User(username, passwordEncoder.encode(password)));

        // Register and sign the new account straight in, rather than making
        // them submit the login form a second time with what they just typed.
        return authenticate(username, password, servletRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String username = request.username() == null ? "" : request.username().trim();
        String password = request.password() == null ? "" : request.password();
        return authenticate(username, password, servletRequest);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        // Reachable only when authenticated — SecurityConfig requires auth on
        // this path, so an anonymous caller never gets past the entry point
        // above to reach this method at all.
        return new AuthResponse(authentication.getName());
    }

    /** Shared by register (auto-login) and login: authenticates, then persists the result into the session. */
    private AuthResponse authenticate(String username, String password, HttpServletRequest servletRequest) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication result;
        try {
            result = authenticationManager.authenticate(authRequest);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(result);
        SecurityContextHolder.setContext(context);

        // Explicitly store the context in the session under the key Spring Security
        // looks for on later requests. This is the documented approach for logins
        // performed outside the standard filter chain (i.e. from our own controller).
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return new AuthResponse(result.getName());
    }
}
