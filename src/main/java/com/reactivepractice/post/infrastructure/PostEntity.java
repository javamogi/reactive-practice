package com.reactivepractice.post.infrastructure;

import com.reactivepractice.post.doamin.Post;
import com.reactivepractice.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("posts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PostEntity {

    @Id
    private Long id;

    private Long userId;

    private String title;

    private String contents;

    public static PostEntity from(Post post) {
        return PostEntity.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .title(post.getTitle())
                .contents(post.getContents())
                .build();
    }

    public Post toModel() {
        return Post.builder()
                .id(id)
                .title(title)
                .contents(contents)
                .build();
    }
}
