package com.reactivepractice.user.controller.port;

import com.reactivepractice.user.domain.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> register(User user);

    Mono<User> findByEmail(String email);
}
