package com.reactivepractice.comment.infrastructure;

import com.reactivepractice.comment.domain.Comment;
import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.domain.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CommentEntity {
    @Id
    private Long id;
    private Long postId;
    private Long userId;
    private String contents;

    public static CommentEntity from(Comment comment) {
        return CommentEntity.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getWriter().getId())
                .contents(comment.getContents())
                .build();
    }

    public Comment toModel() {
        return Comment.builder()
                .id(id)
                .post(Post.builder().id(postId).build())
                .writer(User.builder().id(userId).build())
                .contents(contents)
                .build();
    }
}
