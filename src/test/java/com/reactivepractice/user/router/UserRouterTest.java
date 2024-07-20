package com.reactivepractice.user.router;

import com.reactivepractice.common.PasswordEncoder;
import com.reactivepractice.exception.ErrorCode;
import com.reactivepractice.exception.NotFoundException;
import com.reactivepractice.exception.UnauthorizedException;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.service.port.UserRepository;
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
class UserRouterTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeAll
    static void init(@Autowired UserRepository userRepository,
                     @Autowired PasswordEncoder passwordEncoder){
        userRepository.save(User.builder()
                .email("test@test.test")
                .password(passwordEncoder.encode("test"))
                .build()).subscribe();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void register(){
        UserRequest request = UserRequest.builder()
                .email("test2@test.test")
                .password("test2")
                .build();
        webTestClient
                .post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(2);
                    assertThat(user.getEmail()).isEqualTo("test2@test.test");
                });
    }

    @Test
    @DisplayName("회원 가입 실패 중복된 이메일")
    void failedRegister(){
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        webTestClient
                .post().uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("이메일로 회원 찾기")
    void findByEmail(){
        String request = "test@test.test";
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("email", request)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("이메일로 회원 찾기 이메일 입력값 없음")
    void findByEmailWhenEmptyEmail(){
        webTestClient
                .get().uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("회원 목록")
    void findAll(){
        webTestClient
                .get().uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(1);
    }

    @Test
    @DisplayName("로그인")
    void login(){
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("로그인 실패 가입되지 않은 이메일")
    void failedLoginWhenNotFoundUser(){
        UserRequest request = UserRequest.builder()
                .email("test99@test.test")
                .password("test")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(NotFoundException.class).value(ex -> {
                   assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                   assertThat(ex.getMessage()).isEqualTo(ErrorCode.NOT_FOUND.getMessage());
                });
    }

    @Test
    @DisplayName("로그인 실패 비밀번호 틀림")
    void failedLoginWhenNotMatchingPassword(){
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test2")
                .build();
        webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(UnauthorizedException.class).value(ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
                    assertThat(ex.getMessage()).isEqualTo(ErrorCode.UNAUTHORIZED.getMessage());
                });
    }

    @Test
    @DisplayName("로그인 회원 정보")
    void getLoginUserInfo() {
        UserRequest request = UserRequest.builder()
                .email("test@test.test")
                .password("test")
                .build();

        EntityExchangeResult<UserResponse> loginResult = webTestClient
                .post().uri("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists("SESSION")
                .expectBody(UserResponse.class)
                .returnResult();

        String sessionId = loginResult.getResponseHeaders().getFirst(HttpHeaders.SET_COOKIE);
        sessionId = sessionId.split(";")[0].split("=")[1];

        webTestClient
                .get().uri("/users/login/info")
                .accept(MediaType.APPLICATION_JSON)
                .cookie("SESSION", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).value(user -> {
                    assertThat(user.getId()).isEqualTo(1);
                    assertThat(user.getEmail()).isEqualTo("test@test.test");
                });
    }

    @Test
    @DisplayName("로그인 회원 없음")
    void notLoginUser() {
        webTestClient
                .get().uri("/users/login/info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

}
