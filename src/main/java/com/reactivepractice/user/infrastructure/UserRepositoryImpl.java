package com.reactivepractice.user.infrastructure;

import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserReactiveRepository userReactiveRepository;

    @Override
    public Mono<User> save(User user) {
        return userReactiveRepository.save(user);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userReactiveRepository.findByEmail(email);
    }
}
