package com.scaler.userservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailDto {
    private String fromEmail;
    private String toEmail;
    private String subject;
    private String body;
}
