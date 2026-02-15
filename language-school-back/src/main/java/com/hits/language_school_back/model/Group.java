package com.hits.language_school_back.model;

import com.hits.language_school_back.enums.Difficulty;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Difficulty difficulty;

    @ManyToMany
    private List<User> users;
}
