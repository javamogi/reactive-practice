package com.reactivepractice.user.service;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.exception.DuplicationException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.user.handler.port.UserService;
import com.reactivepractice.user.handler.response.UserResponse;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserResponse> register(UserRequest request){
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> Mono.<User>error(new DuplicationException()))
                .switchIfEmpty(Mono.defer(() -> userRepository.save(User.from(request, passwordEncoder))))
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

    @Override
    public Mono<UserResponse> login(UserRequest request){
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException())))
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(UserResponse::of)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedException())))
                .cache();
    }

}
