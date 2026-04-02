package com.hoang.backend.modules.users.repository;

/**
 * Repository thao tác dữ liệu Account.
 */

import com.hoang.backend.modules.users.entity.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);
}