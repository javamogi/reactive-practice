package com.reactivepractice.user.controller;

import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.controller.response.UserResponse;
import com.reactivepractice.user.domain.UserRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Builder
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public Mono<ResponseEntity<UserResponse>> register(@RequestBody Mono<UserRequest> user){
        return user.flatMap(userService::register)
                .map(u -> ResponseEntity.status(HttpStatus.CREATED).body(u))
                .onErrorResume(DuplicateKeyException.class,
                        error -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<UserResponse>> getUserByEmail(@RequestParam(name = "email") String email){
        return Mono.just(email)
                .flatMap(userService::findByEmail)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


}
