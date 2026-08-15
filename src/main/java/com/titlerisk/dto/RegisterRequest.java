package com.titlerisk.dto;

/** Request body for {@code POST /api/auth/register}. */
public record RegisterRequest(String username, String password) {
}
