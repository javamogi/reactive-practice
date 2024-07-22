package com.reactivepractice.user.handler.request;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LoginRequest {
    private String email;
    private String password;
}
