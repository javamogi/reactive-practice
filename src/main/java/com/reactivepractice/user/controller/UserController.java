package com.reactivepractice.user.controller;

import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    @PostMapping("")
    public ResponseEntity<Mono<User>> register(@RequestBody User user){
        Mono<User> result = userService.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}
