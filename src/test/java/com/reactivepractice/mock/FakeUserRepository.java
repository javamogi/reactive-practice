package com.reactivepractice.mock;

import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.infrastructure.UserEntity;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class FakeUserRepository implements UserRepository {

    private final AtomicLong autoGeneratedId = new AtomicLong(0);
    private final List<UserEntity> data = new CopyOnWriteArrayList<>();

    @Override
    public synchronized Mono<User> save(User user) {
        if (data.stream().anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail()))) {
            return Mono.error(new DuplicateKeyException("already user"));
        }
        if(user.getId() == null || user.getId().equals(0)){
            UserEntity newUser = UserEntity.builder()
                    .id(autoGeneratedId.incrementAndGet())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .build();
            data.add(newUser);
            return Mono.just(newUser.toModel());
        } else {
            data.removeIf(u -> Objects.equals(u.getId(), user.getId()));
            data.add(UserEntity.from(user));
            return Mono.just(user);
        }
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return Flux.fromIterable(data)
                .filter(user -> user.getEmail().equals(email))
                .map(UserEntity::toModel)
                .next();
    }

}
