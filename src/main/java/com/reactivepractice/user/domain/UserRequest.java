package com.reactivepractice.user.domain;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserRequest {
    private Long id;
    private String email;
    private String password;
}
