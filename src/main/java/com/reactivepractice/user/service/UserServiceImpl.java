package com.reactivepractice.user.service;

import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Builder
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> register(User user){
        return userRepository.save(user);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
