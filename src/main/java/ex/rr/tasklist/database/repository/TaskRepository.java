package ex.rr.tasklist.database.repository;

import ex.rr.tasklist.database.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = "SELECT t.* FROM task t, task_list_tasks tlt WHERE t.id=tlt.tasks_id AND tlt.task_list_id=?1", nativeQuery = true)
    List<Task> findAllByTaskListId(Long id);

    @Query(value = "SELECT DISTINCT t.* FROM task t, task_list_tasks tlt WHERE tlt.task_list_id=?1 AND t.id=?2", nativeQuery = true)
    Optional<Task> getTaskIfBelongsToList(Long listId, Long taskId);

}
