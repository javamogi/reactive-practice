package com.reactivepractice.user.domain;

import com.reactivepractice.common.PasswordEncoder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class User {
    private Long id;
    private String email;
    private String password;

    public static User from(UserRequest user, PasswordEncoder passwordEncoder) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .build();
    }

}
