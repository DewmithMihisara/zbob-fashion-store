package com.zbob.modules.users.repository;

import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByRoleIn(Collection<Role> roles, Pageable pageable);

    long countByRoleAndActiveTrue(Role role);
}
