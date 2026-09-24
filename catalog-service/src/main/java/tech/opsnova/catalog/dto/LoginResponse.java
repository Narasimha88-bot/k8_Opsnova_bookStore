package tech.opsnova.catalog.dto;

public record LoginResponse(String token, String username, String role) {
}
