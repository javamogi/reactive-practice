package com.reactivepractice.user.controller;

import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.domain.User;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Builder
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public Mono<ResponseEntity<User>> register(@RequestBody Mono<User> user){
        return user.flatMap(userService::register)
                .map(u -> ResponseEntity.status(HttpStatus.CREATED).body(u));
    }

    @GetMapping("/{email}")
    public Mono<ResponseEntity<User>> getUserByEmail(@PathVariable(name = "email") Mono<String> email){
        return email.flatMap(userService::findByEmail)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
