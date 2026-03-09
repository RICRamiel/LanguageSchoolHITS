package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {
    List<Group> findByUsersId(Long teacherId);

    Group findByName(String s);
}
