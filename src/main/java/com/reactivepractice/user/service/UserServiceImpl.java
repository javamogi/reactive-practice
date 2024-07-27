package com.reactivepractice.user.service;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.exception.model.DuplicationException;
import com.reactivepractice.exception.model.UnauthorizedException;
import com.reactivepractice.exception.model.NotFoundException;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.handler.port.UserService;
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
    public Mono<User> register(UserRequest request){
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> Mono.<User>error(new DuplicationException()))
                .switchIfEmpty(Mono.defer(() -> userRepository.save(User.from(request, passwordEncoder))))
                .cache();
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> login(LoginRequest request){
        return userRepository.findByEmail(request.getEmail())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException())))
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UnauthorizedException())))
                .cache();
    }

    @Override
    public Mono<User> modify(UserRequest request) {
        return userRepository.findById(request.getId())
                .flatMap(user -> userRepository.save(User.from(request, passwordEncoder)));
    }

    @Override
    public Mono<Void> delete(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException())))
                .flatMap(user -> userRepository.deleteById(user.getId()));
    }

}
