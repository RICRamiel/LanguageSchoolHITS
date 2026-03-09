package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.NotificationDto;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification model) {
        return NotificationDto.builder()
                .id(model.getId())
                .text(model.getText())
                .createdByTeacherWithId(model.getCreatedBy().getId())
                .creationDate(model.getCreationDate())
                .groupId(model.getGroup().getId())
                .build();
    }
}