package com.reactivepractice.user.service.port;

import com.reactivepractice.user.domain.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);

    Mono<User> findByEmail(String email);
}
