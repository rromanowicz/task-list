package ex.rr.tasklist;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

@SuppressWarnings({"unused","OptionalGetWithoutIsPresent"})
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskListRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskListRepositoryTest.class);
    private static TaskList taskList;

    @Autowired
    private TaskListRepository repository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(1)
    public void injectedComponentsAreNotNull() {
        assertThat(repository).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @Order(2)
    public void shouldCreateDbTaskList() {
        taskList = createTaskListRecord();
        assertThat(taskList).isNotNull();
    }


    @Test
    @Order(3)
    public void shouldReturnAllTaskLists() {
        List<TaskList> lists = repository.findAll();
        assertThat(lists).isNotEmpty();
        assertThat(lists).hasSizeGreaterThan(0);
    }

    @Test
    @Order(4)
    public void shouldReturn1TaskList() {
        Optional<TaskList> list = repository.findById(taskList.getId());
        assertThat(list.orElse(null)).isNotNull();
    }

    @Test
    @Order(5)
    public void listShouldHaveTasks() {
        Optional<TaskList> list = repository.findById(taskList.getId());
        logger.debug(list::toString);
        List<Task> tasks = list.get().getTasks();
        assertThat(tasks).hasSize(2);
    }

    @Test
    @Order(6)
    public void shouldReturnUserLists() {
        List<TaskList> userLists = repository.findAllByUser("user1");
        assertThat(userLists).hasSizeGreaterThan(0);
    }

    @Test
    @Order(7)
    public void shouldReturnListsSharedWithUser() {
        List<TaskList> userLists = repository.findAllByUser("user2");
        assertThat(userLists).hasSizeGreaterThan(0);
    }

    @Test
    @Order(8)
    public void shouldDeleteTaskList() {
        repository.deleteById(taskList.getId());
        assertThat(repository.findById(taskList.getId())).isEmpty();
    }


    private TaskList createTaskListRecord() {
        TaskList tl = createTaskList();

        return repository.save(tl);
    }

    private TaskList createTaskList() {
        List<Task> tasks = new ArrayList<>();
        Task t1 = Task.builder().taskName("test1").build().toBuilder().build();
        Task t2 = Task.builder().taskName("test2").build().toBuilder().build();
        tasks.add(t1);
        tasks.add(t2);

        User u1 = userRepository.findByUsername("user1").orElse(null);
        User u2 = userRepository.findByUsername("user2").orElse(null);
        List<User> sharedWith = new ArrayList<>();
        sharedWith.add(u2);


        return TaskList.builder()
                .listName("list1")
                .listDescription("desc1")
                .tasks(tasks)
                .owner(u1)
                .sharedWith(sharedWith)
                .build()
                .toBuilder()
                .build();
    }
}
