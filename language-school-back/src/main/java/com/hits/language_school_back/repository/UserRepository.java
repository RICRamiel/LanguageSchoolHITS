package com.hits.language_school_back.repository;

import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findAllByRole(Role role);

    Boolean existsByEmail(String email);

    Optional<User> findByIdAndRole(UUID id, Role role);

    @Query("SELECT COUNT(e) = :size FROM User e WHERE e.id IN :ids")
    boolean existsAllById(@Param("ids") List<UUID> ids, @Param("size") long size);
}
