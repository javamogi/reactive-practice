package com.reactivepractice.user.handler.port;

import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> register(UserRequest request);

    Mono<User> findByEmail(String email);

    Mono<User> findById(Long id);

    Flux<User> findAll();

    Mono<User> login(LoginRequest request);
}
