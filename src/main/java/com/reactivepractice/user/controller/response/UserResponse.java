package com.reactivepractice.user.controller.response;

import com.reactivepractice.user.domain.User;
import lombok.*;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
