package com.reactivepractice.comment.handler.response;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.post.hadler.response.PostResponse;
import com.reactivepractice.user.handler.response.UserResponse;
import lombok.*;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private UserResponse writer;
    private PostResponse post;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContents())
                .writer(UserResponse.of(comment.getWriter()))
                .post(PostResponse.fromWithoutWriter(comment.getPost()))
                .build();
    }

    public static CommentResponse fromWithoutPost(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContents())
                .writer(UserResponse.of(comment.getWriter()))
                .build();
    }

}
