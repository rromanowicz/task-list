package ex.rr.tasklist.database.repository;

import ex.rr.tasklist.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String name);

//    @Query(value = "SELECT u.* FROM 'user' u WHERE u.username=?1 OR u.username IN (?2)", nativeQuery = true)
    @Query(value = "SELECT u.* FROM 'user' u WHERE u.username IN (?1)", nativeQuery = true)
    List<User> findByUsernames(List<String> names);

    @Query(value = "SELECT ht.token FROM hash_token ht WHERE ht.active=1", nativeQuery = true)
    List<String> findActiveTokens();

    @Query(value = "SELECT U.* FROM 'user' u, hash_token ht , user_hash_tokens uht WHERE u.id=uht.user_id AND ht.id=uht.hash_tokens_id AND ht.token=?1", nativeQuery = true)
    Optional<User> findUserByToken(String token);

    @Query(value = "" +
            "SELECT EXISTS (" +
            "SELECT 1 FROM 'user' u, user_hash_tokens uht, hash_token ht, user_roles ur, 'role' r \n" +
            "WHERE u.id=uht.user_id AND  uht.hash_tokens_id=ht.id AND u.id=ur.user_id AND ur.roles_id=r.id\n" +
            "AND (u.username=?2 AND ht.token=?1 OR r.'role'='ADMIN')" +
            ")"
            , nativeQuery = true)
    Integer hasUserAccess(String hash, String username);

    @Query(value = "" +
            "SELECT EXISTS (" +
            "SELECT 1 FROM 'user' u, user_hash_tokens uht, hash_token ht, user_roles ur, 'role' r \n" +
            "WHERE u.id=uht.user_id AND  uht.hash_tokens_id=ht.id AND u.id=ur.user_id AND ur.roles_id=r.id\n" +
            "AND (u.id=?2 AND ht.token=?1 OR r.'role'='ADMIN')" +
            ")"
            , nativeQuery = true)
    Integer hasUserAccess(String hash, Long userId);

}
