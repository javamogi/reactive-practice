package com.reactivepractice.user.controller;

import com.reactivepractice.user.controller.port.UserService;
import com.reactivepractice.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public Mono<ResponseEntity<User>> register(@RequestBody User user){
        return userService.register(user)
                .map(u -> ResponseEntity.status(HttpStatus.CREATED).body(u));
    }

    @GetMapping("/{email}")
    public Mono<ResponseEntity<User>> register(@PathVariable(name = "email") String email){
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
