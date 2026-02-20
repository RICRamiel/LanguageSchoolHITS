package com.hits.language_school_back.dto;

import com.hits.language_school_back.model.Comment;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskTeacherDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private List<Comment> commentList;
}
