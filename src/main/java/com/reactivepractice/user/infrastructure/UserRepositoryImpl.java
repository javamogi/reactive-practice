package com.reactivepractice.user.infrastructure;

import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserReactiveRepository userReactiveRepository;

    @Override
    public Mono<User> save(User user) {
        return userReactiveRepository.save(UserEntity.from(user))
                .flatMap(u -> Mono.just(u.toModel()));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userReactiveRepository.findByEmail(email)
                .flatMap(user -> Mono.just(user.toModel()));
    }

    @Override
    public Mono<User> findById(Long id) {
        return userReactiveRepository.findById(id)
                .flatMap(user -> Mono.just(user.toModel()));
    }

    @Override
    public Flux<User> findAll() {
        return userReactiveRepository.findAll()
                .flatMap(user -> Mono.just(user.toModel()));
    }
}
