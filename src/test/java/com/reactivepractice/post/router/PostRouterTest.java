package com.reactivepractice.post.router;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.post.doamin.PostRequest;
import com.reactivepractice.post.hadler.response.PostResponse;
import com.reactivepractice.post.service.port.PostRepository;
import com.reactivepractice.user.handler.request.LoginRequest;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.service.port.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostRouterTest {

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void init(@Autowired UserRepository userRepository,
                     @Autowired PasswordEncoder passwordEncoder,
                     @Autowired PostRepository postRepository){
        userRepository.save(User.builder()
                .email("test@test.test")
                .password(passwordEncoder.encode("test"))
                .name("테스트")
                .build()).subscribe(user -> postRepository.save(Post.builder()
                        .user(user)
                        .contents("내용")
                        .title("제목")
                .build()).subscribe());
    }

    @AfterAll
    static void end(@Autowired PostRepository postRepository){
        postRepository.deleteALl().subscribe();
    }

    @Test
    @DisplayName("게시글 등록")
    void registerPost() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];


        PostRequest postRequest = PostRequest.builder()
                .title("제목2")
                .contents("내용2")
                .build();
        webTestClient
                .post().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PostResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(2);
                    assertThat(post.getTitle()).isEqualTo("제목2");
                    assertThat(post.getContent()).isEqualTo("내용2");
                });
    }

    @Test
    @DisplayName("게시글 등록 실패 로그인 회원 없음")
    void failedRegisterPostWhenNotLogin() {

        PostRequest postRequest = PostRequest.builder()
                .title("제목")
                .contents("내용")
                .build();
        webTestClient
                .post().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(postRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 조회")
    void getPost() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(1))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PostResponse.class).value(post -> {
                    assertThat(post.getId()).isEqualTo(1);
                    assertThat(post.getTitle()).isEqualTo("제목");
                    assertThat(post.getContent()).isEqualTo("내용");
                    assertThat(post.getWriter().getId()).isEqualTo(1);
                    assertThat(post.getWriter().getEmail()).isEqualTo("test@test.test");
                    assertThat(post.getWriter().getName()).isEqualTo("테스트");
                });
    }

    @Test
    @DisplayName("없는 게시글 조회")
    void getPostWhenEmpty() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(99))
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("게시글 조회 로그인하지 않음")
    void getPostWhenNotLogin() {
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/posts/{id}")
                        .build(2))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void findAll(){
        webTestClient
                .get().uri("/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PostResponse.class)
                .hasSize(2);
    }
}