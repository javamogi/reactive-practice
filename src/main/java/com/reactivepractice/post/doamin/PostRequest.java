package com.reactivepractice.post.doamin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    private Long id;

    private String title;

    private String contents;
}
