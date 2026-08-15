package com.titlerisk.dto;

/** Response body for the auth endpoints — just enough for the frontend to greet the user by name. */
public record AuthResponse(String username) {
}
