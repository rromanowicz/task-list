package ex.rr.tasklist.database.repository;

import ex.rr.tasklist.database.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    Optional<TaskList> findById(Long id);

    List<TaskList> findAll();

    @Query(value = "SELECT tl.* FROM task_list tl JOIN user u ON tl.owner_id=u.id AND u.username=?1 UNION SELECT tl.* FROM task_list tl JOIN task_list_shared_with tlsw ON tl.id =tlsw.task_list_id JOIN user u ON tlsw.shared_with_id =u.id AND u.username=?1", nativeQuery = true)
    List<TaskList> findAllByUser(String user);

    @Query(value="" +
            "WITH RECURSIVE\n" +
            "  uid(id, 'role') AS (\n" +
            "    SELECT u.id, r.'role' FROM 'user' u, hash_token ht , user_hash_tokens uht, user_roles ur, 'role' r\n" +
            "    WHERE u.id=uht.user_id AND ht.id=uht.hash_tokens_id AND u.id=ur.user_id AND ur.roles_id=r.id AND ht.token=?1\n" +
            "  )\n" +
            "SELECT EXISTS(SELECT 1 FROM task_list tl\n" +
            "LEFT JOIN task_list_shared_with tlsw ON tl.id=tlsw.task_list_id\n" +
            "LEFT JOIN uid ON 1=1\n" +
            "WHERE tl.id=?2\n" +
            "AND (uid.'role'='ADMIN' OR (tl.owner_id=uid.id OR tlsw.task_list_id=uid.id))) "
            , nativeQuery = true)
    Integer hasListAccess(String hash, Long listId);

}
