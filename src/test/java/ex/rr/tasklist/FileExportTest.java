package ex.rr.tasklist;

import com.fasterxml.jackson.databind.ObjectMapper;
import ex.rr.tasklist.database.entity.Task;
import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.entity.User;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.TaskRepository;
import ex.rr.tasklist.database.repository.UserRepository;
import ex.rr.tasklist.database.response.TaskListResponse;
import ex.rr.tasklist.files.FileExport;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileExportTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private FileExport fileExport;

    private static Path exportedFilePath;
    private static TaskList taskList;


    @Test
    @Order(0)
    public void shouldCreateTaskList() throws Exception {
        taskList = createTaskListRecord();
        assertThat(taskList).isNotNull();
    }


    @Test
    @Order(1)
    public void shouldCreateTxtFile() throws IOException {

        Optional<TaskList> tempTaskList = taskListRepository.findById(taskList.getId());
        if (tempTaskList.isPresent()) {
            exportedFilePath = fileExport.exportTxt("test.txt", tempTaskList);
            File exportedFile = exportedFilePath.toFile();
            FileInputStream fileInputStream = new FileInputStream(exportedFile);
            fileInputStream.close();
        }
        assert exportedFilePath != null;
        assertThat(exportedFilePath.toFile()).exists();
    }

    @Test
    @Order(2)
    public void shouldDeleteTxtFile() {
        assertThat(exportedFilePath.toFile().delete()).isTrue();
        assertThat(exportedFilePath.toFile()).doesNotExist();
    }

    @Test
    @Order(3)
    public void shouldDeleteTaskList() {
        taskListRepository.deleteById(taskList.getId());
        assertThat(taskListRepository.findById(taskList.getId())).isEmpty();
    }




    private TaskList createTaskListRecord() {
        TaskList tl = createTaskList();
        return taskListRepository.save(tl);
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
