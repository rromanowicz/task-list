package ex.rr.tasklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Controller
@ComponentScan
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private taskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("I'm a teapot.");
    }

    @PostMapping("/api/user/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userRepository.saveAndFlush(user));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/user/id/{id}/get")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(value))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/user/name/{name}/get")
    public ResponseEntity<User> getUserByName(@PathVariable("name") String name) {
        Optional<User> user = userRepository.findByName(name);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(value))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/user/id/{id}/delete")
    public ResponseEntity<User> deleteUserById(@PathVariable("id") Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/user/name/{name}/delete")
    public ResponseEntity<User> deleteUserByName(@PathVariable("name") String name) {
        Optional<User> user = userRepository.findByName(name);
        if(user.isPresent()) {
            userRepository.deleteById(user.get().getId());
            return ResponseEntity.status(HttpStatus.OK).build();
        } else  {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/api/taskList/create")
    public ResponseEntity<TaskList> createTaskList(@RequestBody TaskList taskList) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskListRepository.save(taskList));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/taskList/get/id/{id}")
    public ResponseEntity<TaskList> getTaskListById(@PathVariable Long id) {
        Optional<TaskList> taskList = taskListRepository.findById(id);
        return taskList.map(list -> ResponseEntity.status(HttpStatus.OK).body(list))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/taskList/get/user/{username}")
    public ResponseEntity<List<TaskList>> getTaskListById(@PathVariable String username) {
        List<TaskList> taskList = taskListRepository.findAllByUser(username);
        if (!taskList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(taskList);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{id}/delete")
    public ResponseEntity<Void> deleteTaskListById(@PathVariable Long id) {
        try {
            taskListRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{id}/share/{username}")
    public ResponseEntity<String> shareTaskList(@PathVariable Long id, @PathVariable String username) {
        Optional<TaskList> taskList = taskListRepository.findById(id);
        Optional<User> user = userRepository.findByName(username);

        if (taskList.isPresent() && user.isPresent()) {
            TaskList tempTaskList = taskList.get();
            tempTaskList.getSharedWith().add(user.get());
            taskListRepository.save(tempTaskList);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            if (!taskList.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TaskList not found");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        }
    }

    @GetMapping("/api/taskList/{id}/unShare/{username}")
    public ResponseEntity<String> unShareTaskList(@PathVariable Long id, @PathVariable String username) {
        Optional<TaskList> taskList = taskListRepository.findById(id);
        Optional<User> user = userRepository.findByName(username);

        if (taskList.isPresent() && user.isPresent()) {
            TaskList tempTaskList = taskList.get();
            tempTaskList.getSharedWith().remove(user.get());
            taskListRepository.save(tempTaskList);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            if (!taskList.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("TaskList not found");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        }
    }

    @PostMapping("/api/taskList/{id}/task/add")
    public ResponseEntity<Task> addTask(@PathVariable Long id, @RequestBody Task task){
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.save(task));
    }

    @GetMapping("/api/taskList/{id}/task/getAll")
    public ResponseEntity<List<Task>> getTaskListTasks(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.findAllByTaskListId(id));
    }

    @GetMapping("/api/taskList/{id}/task/{id}/delete")
    public ResponseEntity<Void> deleteTask(@PathVariable Long listId, @PathVariable Long taskId){
        Optional<Task> task = taskRepository.getTaskIfBelongsToList(listId, taskId);
        if(task.isPresent()) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
