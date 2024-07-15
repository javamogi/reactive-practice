package com.reactivepractice.mock;

import com.reactivepractice.user.handler.UserHandler;
import com.reactivepractice.user.service.UserServiceImpl;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;

public class TestContainer {

    public final UserRepository userRepository;
    public final UserHandler userHandler;

    @Builder
    public TestContainer() {
        this.userRepository = new FakeUserRepository();
        UserServiceImpl userService = UserServiceImpl.builder()
                .userRepository(userRepository)
                .build();
        this.userHandler = UserHandler.builder()
                .userService(userService)
                .build();
    }
}
