package com.reactivepractice.user.service;

import com.reactivepractice.exception.DuplicationException;
import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserResponse> register(UserRequest request){
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> Mono.<User>error(new DuplicationException()))
                .switchIfEmpty(Mono.defer(() -> userRepository.save(User.from(request))))
                .map(UserResponse::of)
                .cache();
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
