package com.reactivepractice.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Getter
@Builder
public class User {

    @Id
    private Long id;
    private String email;
    private String password;
}
