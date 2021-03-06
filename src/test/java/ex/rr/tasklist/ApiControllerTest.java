package ex.rr.tasklist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import ex.rr.tasklist.database.entity.*;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.TaskRepository;
import ex.rr.tasklist.database.repository.UserRepository;
import ex.rr.tasklist.database.request.UserRequest;
import ex.rr.tasklist.database.response.TaskListResponse;
import ex.rr.tasklist.database.response.UserResponse;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private static final String RUN_ADMIN = "admin";
    private static final String RUN_USER = "user1";
    private static final String RUN_PASSWORD = "password";
    private static final String SHARE_WITH_USER = "admin";
    private static final String RANDOM_USERNAME = "random_username";
    private static final String RANDOM_TASK_NAME = "random_task_name";
    public static final String USER_HASH = "adsff9g876g";
    public static final String ADMIN_HASH = "q5476nsujsb4zert";
    public static final String RANDOM_USER_HASH = "asdzx325q3tag";
    public static final Role ROLE_USER = Role.builder().role("USER").build().toBuilder().build();
    public static final Role ROLE_ADMIN = Role.builder().role("ADMIN").build().toBuilder().build();
    private static final String RANDOM_PASSWORD = "password";
    public static final String ADMIN_CREDENTIALS = "Basic " + Base64.getEncoder().encodeToString((RUN_ADMIN + ":" + RUN_PASSWORD).getBytes());
    public static final String USER_CREDENTIALS = "Basic " + Base64.getEncoder().encodeToString((RUN_USER + ":" + RUN_PASSWORD).getBytes());
    private static TaskListResponse taskList;
    private static UserResponse user;
    private static Task task;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ResponseMapper responseMapper;

    @Test
    @Order(0)
    public void setup() {
        List<String> users = userRepository.findAll().stream().map(User::getUsername).collect(Collectors.toList());

        addDbUser(users, "admin", ROLE_ADMIN, ADMIN_HASH);
        addDbUser(users, "user1", ROLE_USER, USER_HASH);
        addDbUser(users, "user2", ROLE_USER, USER_HASH);

        assertThat(userRepository.findAll()).hasSizeGreaterThan(2);
    }

    private void addDbUser(List<String> dbUsers, String username, Role role, String hashToken) {
        if (!dbUsers.contains(username))
            userRepository.saveAndFlush(User.builder()
                    .username(username)
                    .password(RANDOM_PASSWORD)
                    .roles(Collections.singletonList(role))
                    .hashTokens(Collections.singletonList(HashToken.builder().token(RANDOM_USER_HASH).build().toBuilder().build()))
                    .build().toBuilder().build());
    }

    @Test
    @Order(1)
    public void shouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isIAmATeapot())
                .andExpect(content().string(containsString("I'm a teapot.")));
    }

    @Test
    @Order(2)
    public void shouldCreateUser() throws Exception {
        UserRequest tempUser = UserRequest.builder()
                .username(RANDOM_USERNAME)
                .password(RANDOM_PASSWORD)
                .build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/user/create")
                .header("hash", ADMIN_HASH)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(getRequestJson(tempUser)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        user = new ObjectMapper().readValue(json, UserResponse.class);
    }

    @Test
    @Order(3)
    public void shouldNotCreateUserIfExists() throws Exception {
        UserRequest tempUser = UserRequest.builder()
                .username(RANDOM_USERNAME)
                .password(RANDOM_PASSWORD)
                .build();

        this.mockMvc.perform(post("/api/user/create")
                .header("hash", ADMIN_HASH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempUser)))
                .andDo(print())
                .andExpect(status().isFound());
    }

    @Test
    @Order(4)
    public void shouldReturnUserById() throws Exception {
        this.mockMvc.perform(get("/api/user/id/1")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("username").value("admin"));
    }

    @Test
    @Order(5)
    public void shouldReturnUserByIdForbidden() throws Exception {
        this.mockMvc.perform(get("/api/user/id/1")
                .header("hash", ADMIN_HASH)
                .header("Authorization", USER_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    public void shouldReturnUserByIdNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/id/9999")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    public void shouldReturnUserByName() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("username").value(RANDOM_USERNAME));
    }

    @Test
    @Order(8)
    public void shouldReturnUserByNameForbidden() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME)
                .header("hash", ADMIN_HASH)
                .header("Authorization", USER_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    public void shouldReturnUserByNameNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/name/9999")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    public void shouldDeleteUserById() throws Exception {
        this.mockMvc.perform(get("/api/user/id/" + user.getId() + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @Order(11)
    public void shouldReturnDeleteUserByIdNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/id/" + user.getId() + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12)
    public void shouldReturnDeleteUserByIdUnauthorized() throws Exception {
        this.mockMvc.perform(get("/api/user/id/" + user.getId() + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", USER_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    public void shouldDeleteUserByName() throws Exception {
        shouldCreateUser();
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @Order(14)
    public void shouldReturnDeleteUserByNameNotFound() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    public void shouldReturnDeleteUserByNameUnauthorized() throws Exception {
        this.mockMvc.perform(get("/api/user/name/" + RANDOM_USERNAME + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", USER_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(51)
    public void shouldCreateTaskList() throws Exception {
        TaskListResponse tempTaskList = createTaskList();


        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/create")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempTaskList)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();



        String json = mvcResult.getResponse().getContentAsString();
        taskList = new ObjectMapper().readValue(json, TaskListResponse.class);
        logger.info(taskList.toString());
    }

    @Test
    @Order(52)
    public void shouldReturnTaskListById() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/get/id/" + taskList.getId())
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        TaskListResponse responseTaskList = new ObjectMapper().readValue(json, TaskListResponse.class);

        assertThat(responseTaskList).isEqualTo(taskList);
        assertThat(responseTaskList.getTasks()).hasSize(2);
        assertThat(responseTaskList.getOwner()).isNotNull();
        assertThat(responseTaskList.getCreatedAt()).isNotNull();
        assertThat(responseTaskList.getSharedWith()).hasSize(1);
        assertThat(responseTaskList.getSharedWith().stream().filter(u -> "user2".equals(u.getUsername())).findFirst().orElse(null)).isNotNull();
    }

    @Test
    @Order(53)
    public void shouldReturnTaskListsByUser() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/get/user/" + taskList.getOwner().getUsername())
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        List<TaskListResponse> responseTaskList = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, TaskListResponse.class));
        assertThat(responseTaskList).hasSizeGreaterThan(0);
    }

    @Test
    @Order(54)
    public void shouldShareWithUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/share/" + SHARE_WITH_USER)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        List<User> sharedWith = taskListRepository.findById(taskList.getId()).get().getSharedWith();
        assertThat(sharedWith).hasSize(2);
        assertThat(sharedWith.stream().filter(user -> SHARE_WITH_USER.equals(user.getUsername())).findFirst().orElse(null)).isNotNull();
    }

    @Test
    @Order(55)
    public void shouldReturnShareWithUserListNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/9999/share/" + SHARE_WITH_USER)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("TaskList not found")));
    }

    @Test
    @Order(56)
    public void shouldReturnShareWithUserUserNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/share/" + RANDOM_USERNAME)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @Order(57)
    public void shouldUnShareWithUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/unShare/" + SHARE_WITH_USER)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        List<User> sharedWith = taskListRepository.findById(taskList.getId()).get().getSharedWith();
        assertThat(sharedWith).hasSize(1);
        assertThat(sharedWith.stream().filter(user -> SHARE_WITH_USER.equals(user.getUsername())).findFirst().orElse(null)).isNull();
    }

    @Test
    @Order(58)
    public void shouldAddTask() throws Exception {
        Task tempTask = Task.builder().taskName(RANDOM_TASK_NAME).build().toBuilder().build();

        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/add")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(tempTask)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        String json = mvcResult.getResponse().getContentAsString();
        task = new ObjectMapper().readValue(json, Task.class);
        logger.info(task.toString());
    }

    @Test
    @Order(59)
    public void shouldGetAllTasksForTaskList() throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/getAll")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
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
    @Order(60)
    public void shouldMarkTaskAsCompleted() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/completed/true")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId()).get().getCompleted()).isTrue();
        assertThat(taskRepository.findById(task.getId()).get().getCompletedAt()).isNotNull();
    }

    @Test
    @Order(61)
    public void shouldMarkTaskAsNotCompleted() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/completed/false")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId()).get().getCompleted()).isFalse();
        assertThat(taskRepository.findById(task.getId()).get().getCompletedAt()).isNull();
    }

    @Test
    @Order(62)
    public void shouldUpdateTaskList() throws Exception {
        taskList.setListName("updatedListName");
        taskList.setListDescription("updatedListDescription");

        MvcResult mvcResult = this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/update")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(taskList)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        TaskList updatedTaskList = taskListRepository.findById(taskList.getId()).get();
        assertThat(updatedTaskList.getListName()).isEqualTo("updatedListName");
        assertThat(updatedTaskList.getListDescription()).isEqualTo("updatedListDescription");
        assertThat(updatedTaskList.getUpdatedAt()).isNotEqualTo(taskList.getUpdatedAt());
    }

    @Test
    @Order(63)
    public void shouldReturnUpdateTaskListForbidden() throws Exception {
        this.mockMvc.perform(post("/api/taskList/9999/update")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(taskList)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(64)
    public void shouldUpdateTask() throws Exception {
        task.setTaskName("updatedTaskName");

        this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/update")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(task)))
                .andDo(print())
                .andExpect(status().isCreated());

        Task updatedTask = taskRepository.findById(task.getId()).get();
        assertThat(updatedTask.getTaskName()).isEqualTo("updatedTaskName");
        assertThat(updatedTask.getUpdatedAt()).isNotEqualTo(task.getUpdatedAt());
    }

    @Test
    @Order(65)
    public void shouldReturnUpdateTaskForbidden() throws Exception {
        this.mockMvc.perform(post("/api/taskList/" + taskList.getId() + "/task/9999/update")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson(task)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(66)
    public void shouldCreateFileForId() throws Exception {
        this.mockMvc.perform(get("/api/taskList/id/" + taskList.getId() + "/download/")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(67)
    public void shouldCreateFileForUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/user/" + RUN_USER + "/download/")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(90)
    public void shouldDeleteTask() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/task/" + task.getId() + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    @Order(91)
    public void shouldDeleteTaskListById() throws Exception {
        this.mockMvc.perform(get("/api/taskList/" + taskList.getId() + "/delete")
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(taskListRepository.findById(taskList.getId())).isEmpty();
    }

    @Test
    @Order(92)
    public void shouldReturnDeleteNotFound() throws Exception {
        this.mockMvc.perform(get("/api/taskList/delete/id/" + taskList.getId())
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
        assertThat(taskListRepository.findById(taskList.getId())).isEmpty();
    }

    @Test
    @Order(93)
    public void shouldNotFindTaskListsByUser() throws Exception {
        this.mockMvc.perform(get("/api/taskList/get/user/" + RANDOM_USERNAME)
                .header("hash", ADMIN_HASH)
                .header("Authorization", ADMIN_CREDENTIALS))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    private TaskListResponse createTaskList() {
        List<Task> tasks = new ArrayList<>();
        Task t1 = Task.builder().taskName("test1").build().toBuilder().build();
        Task t2 = Task.builder().taskName("test2").build().toBuilder().build();
        tasks.add(t1);
        tasks.add(t2);

        User u1 = userRepository.findByUsername("user1").orElse(null);
        User u2 = userRepository.findByUsername("user2").orElse(null);
        List<User> sharedWith = new ArrayList<>();
        sharedWith.add(u2);

        TaskList tl = TaskList.builder()
                .listName("list1")
                .listDescription("desc1")
                .tasks(tasks)
                .owner(u1)
                .sharedWith(sharedWith)
                .build()
                .toBuilder().build();


        return responseMapper.mapTaskListResponse(tl);
    }

    private String getRequestJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(object);
    }

}
