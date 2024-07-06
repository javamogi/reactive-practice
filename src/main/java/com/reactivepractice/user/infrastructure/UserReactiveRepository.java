package com.reactivepractice.user.infrastructure;

import com.reactivepractice.user.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserReactiveRepository extends ReactiveCrudRepository<User, Long> {
    Mono<User> findByEmail(String email);
}
