package com.reactivepractice.post.hadler.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.comment.handler.response.CommentResponse;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.handler.response.UserResponse;
import lombok.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private UserResponse writer;
    private List<CommentResponse> comments;
//    private Flux<CommentResponse> comments;

    public static PostResponse fromWithWriter(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContents())
                .writer(UserResponse.of(post.getUser()))
                .comments(Objects.nonNull(post.getComments())
                        ? post.getComments().stream().map(CommentResponse::fromWithoutPost).toList()
                        : new ArrayList<>())
//                .comments(Objects.nonNull(post.getComments()) ? post.getComments().stream().map(CommentResponse::fromWithoutPost).toList() : new ArrayList<>())
//                .comments(Objects.nonNull(post.getComments()) ? post.getComments().map(CommentResponse::fromWithoutPost) : Flux.empty())
//                .comments(Objects.nonNull(post.getComments()) ? post.getComments().flatMap(c -> {
//                    return Mono.just(CommentResponse.from(c));
//                }) : Flux.empty())
//                .comments(Objects.nonNull(post.getComments())
//                        ? post.getComments().collectList().map(commentList -> commentList.stream().map(CommentResponse::fromWithoutPost).collect(Collectors.toList()))
//                        : new ArrayList<>())
                .build();
    }

    public static PostResponse fromWithoutWriter(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContents())
                .build();
    }
}
