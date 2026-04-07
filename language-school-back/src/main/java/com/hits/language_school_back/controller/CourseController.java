package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.CourseCreateDTO;
import com.hits.language_school_back.dto.CourseDTO;
import com.hits.language_school_back.dto.CourseEditDTO;
import com.hits.language_school_back.dto.CourseStudentAddDTO;
import com.hits.language_school_back.infrastructure.CourseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {
    private final CourseServiceImpl courseService;

    @PostMapping("/create")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseCreateDTO dto) {
        return ResponseEntity.ok(courseService.createCourse(dto));
    }

    @DeleteMapping("/delete")
    public void deleteCourse(@RequestParam("courseId") UUID courseId) {
        courseService.deleteCourse(courseId);
    }

    @PutMapping("/{courseId}/edit")
    public ResponseEntity<CourseDTO> editCourse(@PathVariable("courseId") UUID courseId, @RequestBody CourseEditDTO dto) {
        return ResponseEntity.ok(courseService.updateCourse(courseId, dto));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourse() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @PostMapping("/addStudents")
    public ResponseEntity<Boolean> addStudents(@RequestBody CourseStudentAddDTO studentIds) {
        return ResponseEntity.ok(courseService.addStudentsToCourse(studentIds.getCourseId(), studentIds.getStudentIds()));
    }
}
