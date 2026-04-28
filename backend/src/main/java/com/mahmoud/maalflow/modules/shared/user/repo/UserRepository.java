package com.mahmoud.maalflow.modules.shared.user.repo;

import com.mahmoud.maalflow.modules.shared.enums.UserRole;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select u from User u
            where (:role is null or u.role = :role)
            and (
                :search is null
                or trim(:search) = ''
                or lower(u.name) like lower(concat('%', :search, '%'))
                or lower(coalesce(u.email, '')) like lower(concat('%', :search, '%'))
                or lower(coalesce(u.phone, '')) like lower(concat('%', :search, '%'))
            )
            """)
    Page<User> search(@Param("search") String search, @Param("role") UserRole role, Pageable pageable);

    List<User> findByRoleIn(List<UserRole> roles);


}

