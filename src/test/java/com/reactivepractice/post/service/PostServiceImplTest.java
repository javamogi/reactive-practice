package com.reactivepractice.post.service;

import com.reactivepractice.exception.model.ForbiddenException;
import com.reactivepractice.exception.model.NotFoundException;
import com.reactivepractice.exception.model.UnauthorizedException;
import com.reactivepractice.mock.FakeCommentRepository;
import com.reactivepractice.mock.FakePostRepository;
import com.reactivepractice.mock.FakeUserRepository;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class PostServiceImplTest {

    private PostServiceImpl postService;

    @BeforeEach
    void init() {
        FakePostRepository fakePostRepository = new FakePostRepository();
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        FakeCommentRepository fakeCommentRepository  = new FakeCommentRepository();
        this.postService = PostServiceImpl.builder()
                .postRepository(fakePostRepository)
                .userRepository(fakeUserRepository)
                .commentRepository(fakeCommentRepository)
                .build();

        fakeUserRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
                .name("테스트")
                .build());
        fakeUserRepository.save(User.builder()
                .email("test2@test.test")
                .password("test2")
                .name("테스트2")
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
        long userId = 99;

        //when
        Mono<Post> register = postService.register(request, userId);

        //then
        StepVerifier.create(register)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("게시글 ID로 조회")
    void findById(){
        //given
        long postId = 1;

        //when
        Mono<Post> postMono = postService.getPost(postId);

        //then
        StepVerifier.create(postMono)
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(1);
                    assertThat(p.getTitle()).isEqualTo("제목");
                    assertThat(p.getContents()).isEqualTo("내용");
                    assertThat(p.getUser().getId()).isEqualTo(1);
//                    assertThat(p.getUser().getEmail()).isEqualTo("test@test.test");
//                    assertThat(p.getUser().getName()).isEqualTo("테스트");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 ID로 조회 게시글 없음")
    void findByIdWhenEmptyPost(){
        //given
        long postId = 99;

        //when
        Mono<Post> postMono = postService.getPost(postId);

        //then
        StepVerifier.create(postMono)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void findAll(){
        //given
        //when
        Flux<Post> postMono = postService.getAllPosts();

        //then
        StepVerifier.create(postMono)
//                .expectNextCount(1)
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(1);
                    assertThat(p.getTitle()).isEqualTo("제목");
                    assertThat(p.getContents()).isEqualTo("내용");
                    assertThat(p.getUser().getId()).isEqualTo(1);
//                    assertThat(p.getUser().getEmail()).isEqualTo("test@test.test");
//                    assertThat(p.getUser().getName()).isEqualTo("테스트");
                })
//                .assertNext(p -> {
//                    assertThat(p.getId()).isEqualTo(2);
//                    assertThat(p.getTitle()).isEqualTo("제목2");
//                    assertThat(p.getContents()).isEqualTo("내용2");
//                    assertThat(p.getUser().getId()).isEqualTo(1);
//                    assertThat(p.getUser().getEmail()).isEqualTo("test@test.test");
//                    assertThat(p.getUser().getName()).isEqualTo("테스트");
//                })
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("게시글 수정")
    void modify(){
        //given
        PostRequest request = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        long userId = 1;

        //when
        Mono<Post> post = postService.modify(request, userId);

        //then
        StepVerifier.create(post)
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(1);
                    assertThat(p.getTitle()).isEqualTo("제목 수정");
                    assertThat(p.getContents()).isEqualTo("내용 수정");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 수정 실패 존재하지 않는 게시글")
    void failedModifyWhenEmptyPost(){
        //given
        PostRequest request = PostRequest.builder()
                .id(2L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        long userId = 1;

        //when
        Mono<Post> post = postService.modify(request, userId);

        //then
        StepVerifier.create(post)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

    @Test
    @DisplayName("게시글 수정 실패 작성자와 로그인 회원 정보 불일치")
    void failedModifyWhenNotMatchWriter(){
        //given
        PostRequest request = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        long userId = 2;

        //when
        Mono<Post> post = postService.modify(request, userId);

        //then
        StepVerifier.create(post)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("게시글 수정 실패 존재하지 않는 회원")
    void failedModifyWhenEmptyUser(){
        //given
        PostRequest request = PostRequest.builder()
                .id(1L)
                .title("제목 수정")
                .contents("내용 수정")
                .build();
        long userId = 99;

        //when
        Mono<Post> post = postService.modify(request, userId);

        //then
        StepVerifier.create(post)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제")
    void delete() {
        //given
        long postId = 1;
        long userId = 1;

        //when
        Mono<Void> result = postService.delete(postId, userId);

        //then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("게시글 삭제 실패 존재하지 않는 회원")
    void failedDeleteWhenNotFoundUser() {
        //given
        long postId = 1;
        long userId = 99;

        //when
        Mono<Void> result = postService.delete(postId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제 실패 존재하지 않는 게시글")
    void failedDeleteWhenNotFoundPost() {
        //given
        long postId = 99;
        long userId = 1;

        //when
        Mono<Void> result = postService.delete(postId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

    @Test
    @DisplayName("게시글 삭제 실패 작성자와 요청 회원 다름")
    void failedDeleteWhenNotMatchWriter() {
        //given
        long postId = 1;
        long userId = 2;

        //when
        Mono<Void> result = postService.delete(postId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ForbiddenException
                        && throwable.getMessage().equals("FORBIDDEN"))
                .verify();
    }
}