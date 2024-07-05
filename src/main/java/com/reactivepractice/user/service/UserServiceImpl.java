package com.reactivepractice.user.service;

import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;

    public Mono<User> register(User user){
        return userRepository.save(user);
    }

}
