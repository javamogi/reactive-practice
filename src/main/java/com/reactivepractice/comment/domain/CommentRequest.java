package com.reactivepractice.comment.domain;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CommentRequest {
    private Long id;
    private Long postId;
    private String comment;
}
