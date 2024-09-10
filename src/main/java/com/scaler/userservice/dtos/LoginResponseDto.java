package com.scaler.userservice.dtos;

import com.scaler.userservice.models.Token;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
    private Token token;
}
