package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.Difficulty;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_users",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;

    @OneToMany
    private List<Notification> notifications;
}
