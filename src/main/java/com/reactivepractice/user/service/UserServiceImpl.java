package com.reactivepractice.user.service;

import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Builder
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserResponse> register(UserRequest user){
        return userRepository.save(User.from(user))
                .map(UserResponse::of)
                .onErrorResume(DuplicateKeyException.class,
                        error -> Mono.error(new DuplicateKeyException("already exist email " + user.getEmail())));
    }

    @Override
    public Mono<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::of);
    }

    @Override
    public Flux<UserResponse> findAll() {
        return userRepository.findAll()
                .map(UserResponse::of);
    }

}
