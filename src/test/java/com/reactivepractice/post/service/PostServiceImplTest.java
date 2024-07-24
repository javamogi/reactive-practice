package com.reactivepractice.post.service;

import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.mock.FakePostRepository;
import com.reactivepractice.mock.FakeUserRepository;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class PostServiceImplTest {

    private PostServiceImpl postService;

    @BeforeEach
    void init() {
        FakePostRepository fakePostRepository = new FakePostRepository();
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        this.postService = PostServiceImpl.builder()
                .postRepository(fakePostRepository)
                .userRepository(fakeUserRepository)
                .build();

        fakeUserRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .build());
        fakePostRepository.save(Post.builder()
                        .user(User.builder().id(1L).build())
                        .title("제목")
                        .contents("내용")
                .build());
    }

    @Test
    @DisplayName("게시글 등록")
    void register(){
        //given
        PostRequest request = PostRequest.builder()
                .title("제목2")
                .contents("내용2")
                .build();
        long userId = 1;

        //when
        Mono<Post> register = postService.register(request, userId);

        //then
        StepVerifier.create(register)
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(2);
                    assertThat(p.getTitle()).isEqualTo("제목2");
                    assertThat(p.getContents()).isEqualTo("내용2");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 등록 실패 저장되지 않은 회원")
    void failedRegisterWhenNotLogin(){
        //given
        PostRequest request = PostRequest.builder()
                .title("제목2")
                .contents("내용2")
                .build();
        long userId =2;

        //when
        Mono<Post> register = postService.register(request, userId);

        //then
        StepVerifier.create(register)
                .expectError(NotFoundException.class)
                .verify();
    }
}