package at.ac.tuwien.translator.repository;

import at.ac.tuwien.translator.domain.Authority;
import at.ac.tuwien.translator.domain.User;

import java.time.ZonedDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    @Query("select user from User user inner join fetch user.projects projects where projects.id = (:id)")
    List<User> findAllWithProjectId(@Param("id") Long id);

    @Query(value = "select distinct user from User user left join fetch user.authorities",
        countQuery = "select count(user) from User user")
    Page<User> findAllWithAuthorities(Pageable pageable);

    @Query("select distinct user from User user left join fetch user.projects projects left join fetch user.authorities auth where projects.id = :projectId and auth.name in (:authorities)")
    List<User> findByProjectIdAndAuthority(@Param("projectId") Long projectId, @Param("authorities") String... authorities);
}
