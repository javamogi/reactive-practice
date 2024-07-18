package com.reactivepractice.user.handler.port;

import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.domain.UserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserResponse> register(UserRequest request);

    Mono<UserResponse> findByEmail(String email);

    Mono<UserResponse> findById(Long id);

    Flux<UserResponse> findAll();

    Mono<UserResponse> login(UserRequest request);
}
