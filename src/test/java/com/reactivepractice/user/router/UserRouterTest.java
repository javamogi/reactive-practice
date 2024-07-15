package com.reactivepractice.user.router;

import com.reactivepractice.user.handler.response.UserResponse;
import com.reactivepractice.user.domain.User;
import com.reactivepractice.user.domain.UserRequest;
import com.reactivepractice.user.service.port.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRouterTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void init(@Autowired UserRepository userRepository){
        userRepository.save(User.builder()
                .email("test@test.test")
                .password("test")
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
}
