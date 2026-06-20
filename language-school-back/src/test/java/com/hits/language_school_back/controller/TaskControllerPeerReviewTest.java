package com.hits.language_school_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.PeerReviewAccessDTO;
import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewResultDTO;
import com.hits.language_school_back.dto.PeerReviewResultsDTO;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.mapper.TaskMapper;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.PeerReviewService;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerPeerReviewTest {
    @Mock
    private TaskService taskService;
    @Mock
    private PeerReviewService peerReviewService;
    @Mock
    private UserService userService;
    @Mock
    private TaskMapper taskMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID taskId;
    private UUID teacherId;
    private UUID studentId;
    private UserFullDTO teacher;
    private UserFullDTO student;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService, peerReviewService, userService, taskMapper))
                .build();
        objectMapper = new ObjectMapper();
        taskId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        teacher = UserFullDTO.builder().id(teacherId).role(Role.TEACHER).build();
        student = UserFullDTO.builder().id(studentId).role(Role.STUDENT).build();
    }

    @Test
    void enablePeerReviewEndpoint_routesTeacherPayloadToService() throws Exception {
        PeerReviewEnableDTO payload = PeerReviewEnableDTO.builder()
                .peerReviewDistributionType(PeerReviewDistributionType.RANDOM_CIRCLE)
                .peerReviewerVisibleToTeams(true)
                .build();
        Task savedTask = Task.builder().id(taskId).peerReviewEnabled(true).build();
        TaskDTO response = TaskDTO.builder()
                .id(taskId)
                .peerReviewEnabled(true)
                .peerReviewDistributionType(PeerReviewDistributionType.RANDOM_CIRCLE)
                .peerReviewerVisibleToTeams(true)
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(teacher);
        when(peerReviewService.enablePeerReview(eq(taskId), any(PeerReviewEnableDTO.class), eq(teacherId))).thenReturn(savedTask);
        when(taskMapper.toDto(savedTask)).thenReturn(response);

        mockMvc.perform(post("/task/{taskId}/peer-review/enable", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.peerReviewEnabled").value(true))
                .andExpect(jsonPath("$.peerReviewDistributionType").value("RANDOM_CIRCLE"))
                .andExpect(jsonPath("$.peerReviewerVisibleToTeams").value(true));

        ArgumentCaptor<PeerReviewEnableDTO> dtoCaptor = ArgumentCaptor.forClass(PeerReviewEnableDTO.class);
        verify(peerReviewService).enablePeerReview(eq(taskId), dtoCaptor.capture(), eq(teacherId));
        assertThat(dtoCaptor.getValue().getPeerReviewDistributionType()).isEqualTo(PeerReviewDistributionType.RANDOM_CIRCLE);
        assertThat(dtoCaptor.getValue().getPeerReviewerVisibleToTeams()).isTrue();
    }

    @Test
    void manualPeerReviewEndpoint_routesAssignmentPayloadToService() throws Exception {
        UUID reviewerTeamId = UUID.randomUUID();
        UUID reviewedTeamId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        PeerReviewManualAssignmentDTO payload = PeerReviewManualAssignmentDTO.builder()
                .reviewerTeamId(reviewerTeamId)
                .reviewedTeamId(reviewedTeamId)
                .build();
        PeerReviewAssignmentDTO response = PeerReviewAssignmentDTO.builder()
                .id(assignmentId)
                .taskId(taskId)
                .reviewerTeamId(reviewerTeamId)
                .reviewedTeamId(reviewedTeamId)
                .status(PeerReviewAssignmentStatus.ASSIGNED)
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(teacher);
        when(peerReviewService.assignManualPeerReview(eq(taskId), any(PeerReviewManualAssignmentDTO.class), eq(teacherId)))
                .thenReturn(response);

        mockMvc.perform(post("/task/{taskId}/peer-review/manual-assignments", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.reviewerTeamId").value(reviewerTeamId.toString()))
                .andExpect(jsonPath("$.reviewedTeamId").value(reviewedTeamId.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        ArgumentCaptor<PeerReviewManualAssignmentDTO> dtoCaptor = ArgumentCaptor.forClass(PeerReviewManualAssignmentDTO.class);
        verify(peerReviewService).assignManualPeerReview(eq(taskId), dtoCaptor.capture(), eq(teacherId));
        assertThat(dtoCaptor.getValue().getReviewerTeamId()).isEqualTo(reviewerTeamId);
        assertThat(dtoCaptor.getValue().getReviewedTeamId()).isEqualTo(reviewedTeamId);
    }

    @Test
    void confirmPeerReviewResultsEndpoint_routesTeacherConfirmationToService() throws Exception {
        PeerReviewResultsDTO response = PeerReviewResultsDTO.builder()
                .taskId(taskId)
                .peerReviewEnabled(true)
                .results(List.of())
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(teacher);
        when(peerReviewService.confirmPeerReviewResults(taskId, teacherId)).thenReturn(response);

        mockMvc.perform(post("/task/{taskId}/peer-review/confirm", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.peerReviewEnabled").value(true));

        verify(peerReviewService).confirmPeerReviewResults(taskId, teacherId);
    }

    @Test
    void editPeerReviewAssessmentEndpoint_routesTeacherAssessmentPayloadToService() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        UUID criterionId = UUID.randomUUID();
        AssessmentSubmitDTO payload = assessmentPayload(criterionId, 9, "Teacher correction");
        PeerReviewResultDTO response = PeerReviewResultDTO.builder()
                .taskId(taskId)
                .status(PeerReviewAssignmentStatus.TEACHER_EDITED)
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(teacher);
        when(peerReviewService.editPeerReviewAssessment(eq(taskId), eq(assignmentId), any(AssessmentSubmitDTO.class), eq(teacherId)))
                .thenReturn(response);

        mockMvc.perform(put("/task/{taskId}/peer-review/assignments/{assignmentId}/assessment", taskId, assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value("TEACHER_EDITED"));

        ArgumentCaptor<AssessmentSubmitDTO> dtoCaptor = ArgumentCaptor.forClass(AssessmentSubmitDTO.class);
        verify(peerReviewService).editPeerReviewAssessment(eq(taskId), eq(assignmentId), dtoCaptor.capture(), eq(teacherId));
        assertThat(dtoCaptor.getValue().getItems()).singleElement().satisfies(item -> {
            assertThat(item.getCriterionId()).isEqualTo(criterionId);
            assertThat(item.getPoints()).isEqualTo(9);
            assertThat(item.getComment()).isEqualTo("Teacher correction");
        });
    }

    @Test
    void getMyPeerReviewAssignmentEndpoint_routesStudentRequestToService() throws Exception {
        UUID reviewerTeamId = UUID.randomUUID();
        UUID reviewedTeamId = UUID.randomUUID();
        PeerReviewAccessDTO response = PeerReviewAccessDTO.builder()
                .taskId(taskId)
                .reviewerTeamId(reviewerTeamId)
                .reviewedTeamId(reviewedTeamId)
                .status(PeerReviewAssignmentStatus.ASSIGNED)
                .canSubmit(true)
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(student);
        when(peerReviewService.getMyPeerReviewAssignment(taskId, studentId)).thenReturn(response);

        mockMvc.perform(get("/task/{taskId}/peer-review/my-assignment", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.reviewerTeamId").value(reviewerTeamId.toString()))
                .andExpect(jsonPath("$.reviewedTeamId").value(reviewedTeamId.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.canSubmit").value(true));

        verify(peerReviewService).getMyPeerReviewAssignment(taskId, studentId);
    }

    @Test
    void submitMyPeerReviewAssignmentEndpoint_routesStudentAssessmentPayloadToService() throws Exception {
        UUID criterionId = UUID.randomUUID();
        AssessmentSubmitDTO payload = assessmentPayload(criterionId, 8, "Solid solution");
        PeerReviewAccessDTO response = PeerReviewAccessDTO.builder()
                .taskId(taskId)
                .status(PeerReviewAssignmentStatus.SUBMITTED)
                .canSubmit(false)
                .build();

        when(userService.getMe(any(HttpServletRequest.class))).thenReturn(student);
        when(peerReviewService.submitMyPeerReviewAssignment(eq(taskId), any(AssessmentSubmitDTO.class), eq(studentId)))
                .thenReturn(response);

        mockMvc.perform(post("/task/{taskId}/peer-review/my-assignment/submit", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.canSubmit").value(false));

        ArgumentCaptor<AssessmentSubmitDTO> dtoCaptor = ArgumentCaptor.forClass(AssessmentSubmitDTO.class);
        verify(peerReviewService).submitMyPeerReviewAssignment(eq(taskId), dtoCaptor.capture(), eq(studentId));
        assertThat(dtoCaptor.getValue().getItems()).singleElement().satisfies(item -> {
            assertThat(item.getCriterionId()).isEqualTo(criterionId);
            assertThat(item.getPoints()).isEqualTo(8);
            assertThat(item.getComment()).isEqualTo("Solid solution");
        });
    }

    private AssessmentSubmitDTO assessmentPayload(UUID criterionId, Integer points, String comment) {
        return AssessmentSubmitDTO.builder()
                .items(List.of(AssessmentItemRequestDTO.builder()
                        .criterionId(criterionId)
                        .points(points)
                        .comment(comment)
                        .build()))
                .build();
    }
}
