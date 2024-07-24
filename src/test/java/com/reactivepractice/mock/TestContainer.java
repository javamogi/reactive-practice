package com.reactivepractice.mock;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.post.hadler.PostHandler;
import com.reactivepractice.post.service.PostServiceImpl;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.handler.UserHandler;
import com.reactivepractice.user.service.UserServiceImpl;
import com.reactivepractice.user.service.port.UserRepository;
import lombok.Builder;

public class TestContainer {

    public final UserRepository userRepository;
    public final UserHandler userHandler;
    public final PasswordEncoder passwordEncoder;

    public final PostRepository postRepository;
    public final PostHandler postHandler;

    @Builder
    public TestContainer() {
        this.userRepository = new FakeUserRepository();
        this.passwordEncoder = new FakePasswordEncoder();
        UserServiceImpl userService = UserServiceImpl.builder()
                .passwordEncoder(passwordEncoder)
                .userRepository(userRepository)
                .build();
        this.userHandler = UserHandler.builder()
                .userService(userService)
                .build();
        this.postRepository = new FakePostRepository();
        PostServiceImpl postService = PostServiceImpl.builder()
                .postRepository(postRepository)
                .userRepository(userRepository)
                .build();
        this.postHandler = PostHandler.builder()
                .postService(postService)
                .build();
    }
}
