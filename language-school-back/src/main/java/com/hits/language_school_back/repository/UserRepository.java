package com.hits.language_school_back.repository;

import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(Role role);
    Boolean existsByEmail(String email);
    Optional<User> findByIdAndRole(long id, Role role);
}
