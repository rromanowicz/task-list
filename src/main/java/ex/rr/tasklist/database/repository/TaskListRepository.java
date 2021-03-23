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

}
