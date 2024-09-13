package com.scaler.userservice.security.services;

import com.scaler.userservice.models.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

public class CustomGrantedAuthority implements GrantedAuthority {
    private String authority;

    public CustomGrantedAuthority(Role role) {
        this.authority = role.getName();
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
