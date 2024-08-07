package com.reactivepractice.comment.service;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.domain.CommentRequest;
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

class CommentServiceImplTest {

    private CommentServiceImpl commentService;

    @BeforeEach
    void init() {
        FakePostRepository fakePostRepository = new FakePostRepository();
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        FakeCommentRepository fakeCommentRepository = new FakeCommentRepository();
        this.commentService = CommentServiceImpl.builder()
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
        fakeCommentRepository.save(Comment.builder()
                .post(Post.builder().id(1L).build())
                .writer(User.builder().id(1L).build())
                .contents("댓글 등록")
                .build());
    }

    @Test
    @DisplayName("댓글 등록")
    void register(){
        //given
        CommentRequest request = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록2")
                .build();
        long userId = 1;

        //when
        Mono<Comment> register = commentService.register(request, userId);

        //then
        StepVerifier.create(register)
                .assertNext(c -> {
                    assertThat(c.getId()).isEqualTo(2);
                    assertThat(c.getContents()).isEqualTo("댓글 등록2");
                    assertThat(c.getPost().getId()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 등록 실패 가입하지 않은 회원")
    void failedRegisterWhenEmptyUser(){
        //given
        CommentRequest request = CommentRequest.builder()
                .postId(1L)
                .comment("댓글 등록2")
                .build();
        long userId = 99;

        //when
        Mono<Comment> register = commentService.register(request, userId);

        //then
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("댓글 등록 실패 등록되지 않은 게시글")
    void failedRegisterWhenEmptyPost(){
        //given
        CommentRequest request = CommentRequest.builder()
                .postId(2L)
                .comment("댓글 등록2")
                .build();
        long userId = 1;

        //when
        Mono<Comment> register = commentService.register(request, userId);

        //then
        StepVerifier.create(register)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_POST"))
                .verify();
    }

    @Test
    @DisplayName("댓글 ID로 조회")
    void findById(){
        //given
        long commentId = 1;

        //when
        Mono<Comment> commentMono = commentService.getComment(commentId);

        //then
        StepVerifier.create(commentMono)
                .assertNext(c -> {
                    assertThat(c.getId()).isEqualTo(1);
                    assertThat(c.getContents()).isEqualTo("댓글 등록");
                    assertThat(c.getWriter().getId()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("등록되지 않은 댓글 조회")
    void emptyComment(){
        //given
        long commentId = 2;

        //when
        Mono<Comment> commentMono = commentService.getComment(commentId);

        //then
        StepVerifier.create(commentMono)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }

    @Test
    @DisplayName("게시글 댓글 목록 조회")
    void getList(){
        //given
        long postId = 1;

        //when
        Flux<Comment> commentList = commentService.getCommentList(postId);

        //then
        StepVerifier.create(commentList)
//                .expectNextCount(1)
                .assertNext(c -> {
                    assertThat(c.getId()).isEqualTo(1);
                    assertThat(c.getContents()).isEqualTo("댓글 등록");
                    assertThat(c.getPost().getId()).isEqualTo(1);
                    assertThat(c.getWriter().getId()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 수정")
    void modify(){
        //given
        CommentRequest request = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        long userId = 1;

        //when
        Mono<Comment> commentMono = commentService.modify(request, userId);

        //then
        StepVerifier.create(commentMono)
                .assertNext(p -> {
                    assertThat(p.getId()).isEqualTo(1);
                    assertThat(p.getContents()).isEqualTo("댓글 수정");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 수정 실패 존재하지 않는 댓글")
    void failedModifyWhenEmptyComment(){
        //given
        CommentRequest request = CommentRequest.builder()
                .id(2L)
                .comment("댓글 수정")
                .build();
        long userId = 1;

        //when
        Mono<Comment> commentMono = commentService.modify(request, userId);

        //then
        StepVerifier.create(commentMono)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }

    @Test
    @DisplayName("댓글 수정 실패 작성자와 로그인 회원 정보 불일치")
    void failedModifyWhenNotMatchWriter(){
        //given
        CommentRequest request = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        long userId = 2;

        //when
        Mono<Comment> commentMono = commentService.modify(request, userId);

        //then
        StepVerifier.create(commentMono)
                .expectErrorMatches(throwable -> throwable instanceof UnauthorizedException
                        && throwable.getMessage().equals("UNAUTHORIZED"))
                .verify();
    }

    @Test
    @DisplayName("댓글 수정 실패 존재하지 않는 회원")
    void failedModifyWhenEmptyUser(){
        //given
        CommentRequest request = CommentRequest.builder()
                .id(1L)
                .comment("댓글 수정")
                .build();
        long userId = 99;

        //when
        Mono<Comment> commentMono = commentService.modify(request, userId);

        //then
        StepVerifier.create(commentMono)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제")
    void delete() {
        //given
        long commentId = 1;
        long userId = 1;

        //when
        Mono<Void> result = commentService.delete(commentId, userId);

        //then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("댓글 삭제 실패 존재하지 않는 회원")
    void failedDeleteWhenNotFoundUser() {
        //given
        long commentId = 1;
        long userId = 99;

        //when
        Mono<Void> result = commentService.delete(commentId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_USER"))
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제 실패 존재하지 않는 게시글")
    void failedDeleteWhenNotFoundPost() {
        //given
        long commentId = 99;
        long userId = 1;

        //when
        Mono<Void> result = commentService.delete(commentId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException
                        && throwable.getMessage().equals("NOT_FOUND_COMMENT"))
                .verify();
    }

    @Test
    @DisplayName("댓글 삭제 실패 작성자와 요청 회원 다름")
    void failedDeleteWhenNotMatchWriter() {
        //given
        long commentId = 1;
        long userId = 2;

        //when
        Mono<Void> result = commentService.delete(commentId, userId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ForbiddenException
                        && throwable.getMessage().equals("FORBIDDEN"))
                .verify();
    }
}