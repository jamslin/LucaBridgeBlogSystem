package com.lucabridge.blog.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "site_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSetting {

    @Id
    @Column(name = "key", length = 80)
    private String key;

    @Column(name = "value", nullable = false, columnDefinition = "text")
    private String value;
}
