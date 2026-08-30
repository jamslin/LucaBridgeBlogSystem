package com.lucabridge.core.blog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BlogGalleryId implements Serializable {

    @Column(name = "blog_id")
    private Long blogId;

    @Column(name = "media_id")
    private Long mediaId;
}
