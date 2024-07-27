package com.reactivepractice.mock;

import com.reactivepractice.comment.handler.CommentHandler;
import com.reactivepractice.comment.service.CommentServiceImpl;
import com.reactivepractice.comment.service.port.CommentRepository;
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

    public final CommentRepository commentRepository;
    public final CommentHandler commentHandler;

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

        this.commentRepository = new FakeCommentRepository();
        CommentServiceImpl commentService = CommentServiceImpl.builder()
                .commentRepository(commentRepository)
                .userRepository(userRepository)
                .postRepository(postRepository)
                .build();

        this.commentHandler = CommentHandler.builder()
                .commentService(commentService)
                .build();
    }
}
