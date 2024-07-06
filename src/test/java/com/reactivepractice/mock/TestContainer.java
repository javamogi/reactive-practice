package com.reactivepractice.mock;

import com.reactivepractice.user.controller.UserController;
import com.reactivepractice.user.service.UserServiceImpl;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;

public class TestContainer {

    public final UserRepository userRepository;
    public final UserController userController;

    @Builder
    public TestContainer() {
        this.userRepository = new FakeUserRepository();
        UserServiceImpl userService = UserServiceImpl.builder()
                .userRepository(userRepository)
                .build();
        this.userController = UserController.builder()
                .userService(userService)
                .build();
    }
}
