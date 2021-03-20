package ex.rr.tasklist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import junit.framework.TestCase;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"unused", "OptionalGetWithoutIsPresent"})
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiControllerTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(ApiControllerTest.class);
    private static final String SHARE_WITH_USER = "user3";
    private static final String RANDOM_USERNAME = "random_username";
    private static final String RANDOM_TASK_NAME = "random_task_name";
    private static TaskList taskList;
    private static User user;
    private static Task task;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Test
    @Order(0)
    public void shouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isIAmATeapot())
                .andExpect(content().string(containsString("I'm a teapot.")));
    }

    @Test
    @Order(1)
    public void shouldCreateUser() throws Exception {
        User tempUser = User.builder().name(RANDOM_USERNAME).build().toBuilder().build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        user = new ObjectMapper().readValue(json, User.class);
        logger.info(user.toString());
    }

    @Test
    @Order(2)
    public void shouldReturnUserById() throws Exception {
        this.mockMvc.perform(get("/api/user/id/1/get")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value("user1"));
    }

    @Test
    @Order(3)
    public void shouldReturnUserByIdNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/id/9999/get")).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    public void shouldReturnUserByName() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/get")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("name").value(RANDOM_USERNAME));
    }

    @Test
    @Order(5)
    public void shouldReturnUserByNameNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/name/9999/get")).andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(6)
    public void shouldDeleteUserById() throws Exception {
        this.mockMvc.perform(get("/api/user/id/" + user.getId() + "/delete"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @Order(7)
    public void shouldReturnDeleteUserByIdNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/id/" + user.getId() + "/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(8)
    public void shouldDeleteUserByName() throws Exception {
        shouldCreateUser();
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/delete"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @Order(9)
    public void shouldReturnDeleteUserByNameNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/delete"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(11)
    public void shouldCreateTaskList() throws Exception {
        TaskList tempTaskList = createTaskList();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempTaskList)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        taskList = new ObjectMapper().readValue(json, TaskList.class);
        logger.info(taskList.toString());
    }

    @Test
    @Order(12)
    public void shouldReturnTaskListById() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/get/id/" + taskList.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        TaskList responseTaskList = new ObjectMapper().readValue(json, TaskList.class);

        assertThat(responseTaskList).isEqualTo(taskList);
        assertThat(responseTaskList.getTasks()).hasSize(2);
        assertThat(responseTaskList.getOwner()).isNotNull();
        assertThat(responseTaskList.getCreatedAt()).isNotNull();
        assertThat(responseTaskList.getSharedWith()).hasSize(1);
        assertThat(responseTaskList.getSharedWith().stream().filter(user -> "user2".equals(user.getName())).findFirst().orElse(null)).isNotNull();
    }

    @Test
    @Order(13)
    public void shouldReturnTaskListsByUser() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/get/user/" + taskList.getOwner().getName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<TaskList> responseTaskList = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, TaskList.class));
        assertThat(responseTaskList).hasSizeGreaterThan(0);
    }

    @Test
    @Order(14)
    public void shouldShareWithUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/share/" + SHARE_WITH_USER))
                .andDo(print())
                .andExpect(status().isOk());
        List<User> sharedWith = taskListRepository.findById(taskList.getId()).get().getSharedWith();
        assertThat(sharedWith).hasSize(2);
        assertThat(sharedWith.stream().filter(user -> SHARE_WITH_USER.equals(user.getName())).findFirst().orElse(null)).isNotNull();
    }

    @Test
    @Order(15)
    public void shouldReturnShareWithUserListNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/9999/share/" + SHARE_WITH_USER))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("TaskList not found")));
    }

    @Test
    @Order(16)
    public void shouldReturnShareWithUserUserNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/share/" + RANDOM_USERNAME))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @Order(17)
    public void shouldUnShareWithUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/unShare/" + SHARE_WITH_USER))
                .andDo(print())
                .andExpect(status().isOk());
        List<User> sharedWith = taskListRepository.findById(taskList.getId()).get().getSharedWith();
        assertThat(sharedWith).hasSize(1);
        assertThat(sharedWith.stream().filter(user -> SHARE_WITH_USER.equals(user.getName())).findFirst().orElse(null)).isNull();
    }

    @Test
    @Order(18)
    public void shouldAddTask() throws Exception {
        Task tempTask = Task.builder().taskName(RANDOM_TASK_NAME).build().toBuilder().build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempTask)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        task = new ObjectMapper().readValue(json, Task.class);
        logger.info(task.toString());
    }

    @Test
    @Order(19)
    public void shouldGetAllTasksForTaskList() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/getAll"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<Task> tasks = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        assertThat(tasks).hasSize(3);
        assertThat(tasks.stream().filter(task -> RANDOM_TASK_NAME.equals(task.getTaskName())).findFirst().orElse(null)).isNotNull();
    }

    @Test
    @Order(20)
    public void shouldMarkTaskAsCompleted() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/completed/true"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId()).get().getCompleted()).isTrue();
        assertThat(taskRepository.findById(task.getId()).get().getCompletedAt()).isNotNull();
    }

    @Test
    @Order(21)
    public void shouldMarkTaskAsNotCompleted() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/completed/false"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId()).get().getCompleted()).isFalse();
        assertThat(taskRepository.findById(task.getId()).get().getCompletedAt()).isNull();
    }

    @Test
    @Order(22)
    public void shouldUpdateTaskList() throws Exception {
        taskList.setListName("updatedListName");
        taskList.setListDescription("updatedListDescription");

        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(taskList)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        TaskList updatedTaskList = taskListRepository.findById(taskList.getId()).get();
        assertThat(updatedTaskList.getListName()).isEqualTo("updatedListName");
        assertThat(updatedTaskList.getListDescription()).isEqualTo("updatedListDescription");
        assertThat(updatedTaskList.getUpdatedAt()).isNotEqualTo(taskList.getUpdatedAt());
    }

    @Test
    @Order(23)
    public void shouldReturnUpdateTaskListForbidden() throws Exception {
        this.mockMvc.perform(post("/api/taskList/9999/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(taskList)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(24)
    public void shouldUpdateTask() throws Exception {
        task.setTaskName("updatedTaskName");

        this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(task)))
                .andDo(print())
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(task.getId()).get();
        assertThat(updatedTask.getTaskName()).isEqualTo("updatedTaskName");
        assertThat(updatedTask.getUpdatedAt()).isNotEqualTo(task.getUpdatedAt());
    }

    @Test
    @Order(25)
    public void shouldReturnUpdateTaskForbidden() throws Exception {
        this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(task)))
                .andDo(print())
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(task.getId()).get();
        assertThat(updatedTask.getTaskName()).isEqualTo("updatedTaskName");
        assertThat(updatedTask.getUpdatedAt()).isNotEqualTo(task.getUpdatedAt());
    }

    @Test
    @Order(90)
    public void shouldDeleteTask() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/delete"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }


    @Test
    @Order(91)
    public void shouldDeleteTaskListById() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/delete"))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskListRepository.findById(taskList.getId())).isEmpty();
    }

    @Test
    @Order(92)
    public void shouldReturnDeleteNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/delete/id/" + taskList.getId()))
                .andDo(print())
                .andExpect(status().isNotFound());
        assertThat(taskListRepository.findById(taskList.getId())).isEmpty();
    }

    @Test
    @Order(93)
    public void shouldNotFindTaskListsByUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/get/user/" + "some_random_username"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    private TaskList createTaskList() {
        List<Task> tasks = new ArrayList<>();
        Task t1 = Task.builder().taskName("test1").build().toBuilder().build();
        Task t2 = Task.builder().taskName("test2").build().toBuilder().build();
        tasks.add(t1);
        tasks.add(t2);

        User u1 = userRepository.findByName("user1").orElse(null);
        User u2 = userRepository.findByName("user2").orElse(null);
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

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }

}
