package com.reactivepractice.user.service.port;

import com.reactivepractice.user.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);

    Mono<User> findByEmail(String email);

    Mono<User> findById(Long id);

    Flux<User> findAll();
}
