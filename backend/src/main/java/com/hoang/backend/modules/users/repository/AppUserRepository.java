package com.hoang.backend.modules.users.repository;

/**
 * Repository thao tác dữ liệu AppUser.
 */

import com.hoang.backend.modules.users.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findAllByOrderByDateJoinedDesc();
}