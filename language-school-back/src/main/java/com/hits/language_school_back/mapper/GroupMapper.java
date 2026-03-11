package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.GroupAnswerDTO;
import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.TaskRepository;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {
    public GroupAnswerDTO toDto(Group group) {

        return GroupAnswerDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .difficulty(group.getDifficulty())
                .language(new LanguageDTO(group.getLanguage().getName()))
                .build();
    }
}
