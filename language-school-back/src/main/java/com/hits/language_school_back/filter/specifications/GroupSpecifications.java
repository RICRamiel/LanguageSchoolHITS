package com.hits.language_school_back.filter.specifications;

import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Language;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class GroupSpecifications {
    public static Specification<Group> withFilters(GroupFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(filter.getName() != null && !filter.getName().isEmpty()){
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + filter.getName().toLowerCase()+"%"
                ));
            }
            if(filter.getDifficulty() != null){
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), filter.getDifficulty()));
            }
            if(filter.getLanguage() != null && !filter.getLanguage().isEmpty()){
                Join<Group, Language> languageJoin = root.join("language");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(languageJoin.get("name")),
                        "%" + filter.getLanguage().toLowerCase() + "%"
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
