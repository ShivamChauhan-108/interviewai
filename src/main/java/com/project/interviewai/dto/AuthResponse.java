package com.project.interviewai.dto;

import com.project.interviewai.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private String fullName;
    private Role role;
    private String message;
}
