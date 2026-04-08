package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.dto.TaskParticipationDTO;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.repository.AttachmentRepository;
import com.hits.language_school_back.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskParticipationMapper {

    private final AttachmentRepository attachmentRepository;
    private final VoteRepository voteRepository;

    public TaskParticipationDTO toDto(Participation participation) {
        Team team = participation.getTeam();
        List<AttachmentDownloadInfo> attachments = attachmentRepository.findAllByParticipationId(participation.getId()).stream()
                .map(this::mapAttachment)
                .toList();

        return TaskParticipationDTO.builder()
                .id(participation.getId())
                .studentId(participation.getStudent().getId())
                .studentName(participation.getStudent().getFirstName() + " " + participation.getStudent().getLastName())
                .captain(participation.getIsCaptain())
                .mark(participation.getMark())
                .averageMark(participation.getAverageMark())
                .votesCount((int) voteRepository.countByParticipationId(participation.getId()))
                .solutionStatus(participation.getSolutionStatus())
                .submittedAt(participation.getSubmittedAt())
                .selectedSolution(team != null
                        && team.getSolutionParticipation() != null
                        && team.getSolutionParticipation().getId().equals(participation.getId()))
                .attachments(attachments)
                .build();
    }

    private AttachmentDownloadInfo mapAttachment(Attachment attachment) {
        return AttachmentDownloadInfo.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .objectKey(attachment.getObjectKey())
                .build();
    }
}
