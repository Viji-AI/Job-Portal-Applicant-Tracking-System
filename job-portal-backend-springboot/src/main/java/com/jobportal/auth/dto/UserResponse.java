package com.jobportal.auth.dto;

import com.jobportal.auth.entity.Role;
import com.jobportal.auth.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String phone
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone()
        );
    }
}
