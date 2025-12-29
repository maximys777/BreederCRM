package com.maximys777.pugs.security.repository;

import com.maximys777.pugs.security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u.telegramChatId FROM UserEntity u " +
            "JOIN u.roles r " +
            "WHERE u.telegramChatId IS NOT NULL " +
            "AND r IN ('ADMIN', 'OWNER', 'EDITOR')")
    List<Long> findAllPersonalChatsId();
}
