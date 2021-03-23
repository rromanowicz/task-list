package ex.rr.tasklist;

import ex.rr.tasklist.database.entity.Task;
import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskListRepositoryTest.class);
    private static TaskList taskList;

    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskRepository taskRepository;


    @Test
    @Order(0)
    void injectedComponentsAreNotNull() {
        assertThat(taskListRepository).isNotNull();
        assertThat(taskRepository).isNotNull();
    }

    @Test
    @Order(1)
    void shouldCreateTaskList() {
        taskList = createTaskList();
        assertThat(taskList).isNotNull();
    }


    @Test
    @Order(2)
    void shouldReturnAllTasksForTaskListId() {
        List<Task> tasks = taskRepository.findAllByTaskListId(taskList.getId());
        assertThat(tasks).hasSize(2);
    }

    @Test
    @Order(3)
    void shouldReturnNoTasksForTaskListId() {
        List<Task> tasks = taskRepository.findAllByTaskListId(9999L);
        assertThat(tasks).hasSize(0);
    }

    @Test
    @Order(4)
    void shouldReturnTaskBelongingToTaskList() {
        Optional<Task> task = taskRepository.getTaskIfBelongsToList(taskList.getId(), taskList.getTasks().get(0).getId());
        assertThat(task).isNotEmpty();
    }

    @Test
    @Order(5)
    void shouldNotReturnTaskNotBelongingToTaskList() {
        Optional<Task> task = taskRepository.getTaskIfBelongsToList(taskList.getId(), 9999L);
        assertThat(task).isEmpty();
    }

    @Test
    @Order(99)
    public void shouldDeleteTaskList() {
        taskListRepository.deleteById(taskList.getId());
        assertThat(taskRepository.findById(taskList.getId())).isEmpty();
    }


    private TaskList createTaskList() {
        List<Task> tasks = new ArrayList<>();
        Task t1 = Task.builder().taskName("TaskRepositoryTest1").build().toBuilder().build();
        Task t2 = Task.builder().taskName("TaskRepositoryTest2").build().toBuilder().build();
        tasks.add(t1);
        tasks.add(t2);

        TaskList tl = TaskList.builder()
                .listName("TaskRepositoryTest")
                .listDescription("TaskRepositoryTestDesc")
                .tasks(tasks)
                .build()
                .toBuilder()
                .build();

        return taskListRepository.save(tl);
    }


}
