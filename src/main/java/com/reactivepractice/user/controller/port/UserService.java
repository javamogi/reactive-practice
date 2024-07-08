package com.reactivepractice.user.controller.port;

import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.UserRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserResponse> register(UserRequest request);

    Mono<UserResponse> findByEmail(String email);

}
