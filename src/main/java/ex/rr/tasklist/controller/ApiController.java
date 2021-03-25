package ex.rr.tasklist.controller;

import ex.rr.tasklist.FileParser;
import ex.rr.tasklist.ResponseMapper;
import ex.rr.tasklist.database.entity.Task;
import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.entity.User;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.TaskRepository;
import ex.rr.tasklist.database.repository.UserRepository;
import ex.rr.tasklist.database.request.UserRequest;
import ex.rr.tasklist.database.response.TaskListResponse;
import ex.rr.tasklist.database.response.UserResponse;
import ex.rr.tasklist.files.FileExport;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@SuppressWarnings("unused")
@Controller
@ComponentScan
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private List<String> userHash;

    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ResponseMapper responseMapper;
    @Autowired
    private FileParser fileParser;

    @GetMapping("/")
    public ResponseEntity<String> root(
    ) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("I'm a teapot.");
    }

    @PostMapping("/api/user/create")
    public ResponseEntity<UserResponse> createUser(
            @RequestHeader("hash") String hash,
            @RequestBody UserRequest userRequest
    ) {
        User user = User.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build().toBuilder().build();
        user.initToken(hash);

        try {
            if (!userRepository.findByUsername(user.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.mapUserResponse(userRepository.saveAndFlush(user)));
            } else {
                return ResponseEntity.status(HttpStatus.FOUND).build();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/user/id/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader("hash") String hash,
            @PathVariable("userId") Long userId
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<User> user = userRepository.findById(userId);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(responseMapper.mapUserResponse(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/user/name/{username}")
    public ResponseEntity<UserResponse> getUserByName(
            @RequestHeader("hash") String hash,
            @PathVariable("username") String username
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(value -> ResponseEntity.status(HttpStatus.OK).body(responseMapper.mapUserResponse(value)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/user/id/{userId}/delete")
    public ResponseEntity<Void> deleteUserById(
            @RequestHeader("hash") String hash,
            @PathVariable("userId") Long userId
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            userRepository.deleteById(userId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/user/name/{username}/delete")
    public ResponseEntity<Void> deleteUserByName(
            @RequestHeader("hash") String hash,
            @PathVariable("username") String username
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.deleteById(user.get().getId());
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/api/taskList/create")
    public ResponseEntity<TaskListResponse> createTaskList(
            @RequestHeader("hash") String hash,
            @RequestBody TaskListResponse taskList
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        logger.error(taskList.toString());
        try {
            TaskList taskList1 = responseMapper.mapTaskList(taskList);
            logger.error(taskList1.toString());
            TaskList save = taskListRepository.save(taskList1);
            logger.error(save.toString());
            TaskListResponse response = responseMapper.mapTaskListResponse(save);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/taskList/get/id/{listId}")
    public ResponseEntity<TaskListResponse> getTaskListById(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId
    ) {
        if (!hasAccess(hash, listId))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<TaskList> taskList = taskListRepository.findById(listId);
        return taskList.map(list -> ResponseEntity.status(HttpStatus.OK).body(responseMapper.mapTaskListResponse(list)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/api/taskList/get/user/{username}")
    public ResponseEntity<List<TaskListResponse>> getTaskListById(
            @RequestHeader("hash") String hash,
            @PathVariable("username") String username
    ) {
        if (validateHeader(hash)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        List<TaskList> taskLists = taskListRepository.findAllByUser(username);
        if (!taskLists.isEmpty()) {
            List<TaskListResponse> responses = new ArrayList<>();
            taskLists.forEach(it -> responses.add(responseMapper.mapTaskListResponse(it)));
            return ResponseEntity.status(HttpStatus.OK).body(responses);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{listId}/delete")
    public ResponseEntity<Void> deleteTaskListById(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            taskListRepository.deleteById(listId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{listId}/share/{username}")
    public ResponseEntity<String> shareTaskList(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @PathVariable("username") String username
    ) {
        Optional<TaskList> taskList = taskListRepository.findById(listId);
        Optional<User> user = userRepository.findByUsername(username);

        if (taskList.isPresent() && user.isPresent()) {
            if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

    @GetMapping("/api/taskList/{listId}/unShare/{username}")
    public ResponseEntity<String> unShareTaskList(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @PathVariable("username") String username
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<TaskList> taskList = taskListRepository.findById(listId);
        Optional<User> user = userRepository.findByUsername(username);

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

    @PostMapping("/api/taskList/{listId}/task/add")
    public ResponseEntity<Task> addTask(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @RequestBody Task task
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<TaskList> taskList = taskListRepository.findById(listId);
        if (taskList.isPresent()) {
            TaskList list1 = taskList.get();
            list1.getTasks().add(task);
            TaskList updatedTaskList = taskListRepository.save(list1);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.max(updatedTaskList.getTasks(), Comparator.comparing(Task::getCreatedAt)));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{listId}/task/getAll")
    public ResponseEntity<List<Task>> getTaskListTasks(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.status(HttpStatus.OK).body(taskRepository.findAllByTaskListId(listId));
    }

    @GetMapping("/api/taskList/{listId}/task/{taskId}/delete")
    public ResponseEntity<Void> deleteTask(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<Task> task = taskRepository.getTaskIfBelongsToList(listId, taskId);
        if (task.isPresent()) {
            taskRepository.deleteById(taskId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/api/taskList/{listId}/task/{taskId}/completed/{completed}")
    public ResponseEntity<Void> toggleTaskCompleted(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId,
            @PathVariable boolean completed
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Optional<Task> task = taskRepository.getTaskIfBelongsToList(listId, taskId);
        if (task.isPresent()) {
            Task tempTask = task.get();
            if (completed) {
                tempTask.setCompleted(true);
                tempTask.setCompletedAt(System.currentTimeMillis());
            } else {
                tempTask.setCompleted(false);
                tempTask.setCompletedAt(null);
            }
            tempTask.setUpdatedAt(System.currentTimeMillis());
            taskRepository.save(tempTask);
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/api/taskList/{listId}/update")
    public ResponseEntity<Void> updateTaskList(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @RequestBody TaskListResponse taskListResponse
    ) {
        if (listId.equals(taskListResponse.getId())) {
            if (taskListRepository.existsById(listId)) {
                if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                taskListResponse.setUpdatedAt(System.currentTimeMillis());
                taskListRepository.save(responseMapper.mapTaskList(taskListResponse));
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/api/taskList/{listId}/task/{taskId}/update")
    public ResponseEntity<Void> updateTaskListTask(
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId,
            @PathVariable("taskId") Long taskId,
            @RequestBody Task task
    ) {
        if (!hasAccess(hash, listId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (taskId.equals(task.getId()) && taskRepository.getTaskIfBelongsToList(listId, taskId).isPresent()) {
            task.setUpdatedAt(System.currentTimeMillis());
            taskRepository.save(task);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/api/taskList/id/{listId}/download")
    @ResponseBody
    public void downloadTaskList(
            HttpServletResponse response,
            @RequestHeader("hash") String hash,
            @PathVariable("listId") Long listId
    ) {
        if (!hasAccess(hash, listId)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        Optional<TaskList> taskList = taskListRepository.findById(listId);
        try {
            if (taskList.isPresent()) {
                String fileName = String.format("[%s]_%s.txt", taskList.get().getId(), taskList.get().getListName());
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", "attachment; filename=" + fileName);
                response.setStatus(HttpStatus.OK.value());
                OutputStream outputStream = response.getOutputStream();
                generateFile(taskList.get(), fileName, outputStream);
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating file.");
        }
    }

    @GetMapping("/api/taskList/user/{username}/download")
    @ResponseBody
    public void downloadTaskList(
            HttpServletResponse response,
            @RequestHeader("hash") String hash,
            @PathVariable("username") String username
    ) {
        if (validateHeader(hash)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        List<TaskList> taskLists = taskListRepository.findAllByUser(username);
        try {
            if (!taskLists.isEmpty()) {
                String fileName = String.format("[%s]_Lists[%s].txt", username, taskLists.size());
                response.setContentType("application/octet-stream");
                response.setHeader("Content-disposition", "attachment; filename=" + fileName);
                response.setStatus(HttpStatus.OK.value());
                OutputStream outputStream = response.getOutputStream();
                generateFile(taskLists, fileName, outputStream);
            } else {
                response.setStatus(HttpStatus.NOT_FOUND.value());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while creating file.");
        }
    }

    @PostMapping("/api/taskList/upload")
    public ResponseEntity<String> uploadFile(
            @RequestHeader("hash") String hash,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Optional<User> userByToken = userRepository.findUserByToken(hash);
            if (!userByToken.isPresent())
                throw new IllegalAccessException();
            List<TaskList> parsedLists = fileParser.toTaskList(file, responseMapper.mapUserResponse(userByToken.get()));
            taskListRepository.saveAll(parsedLists);
            return ResponseEntity.status(HttpStatus.OK).body(String.format("File '%s', Processed {%s} records.", file.getName(), parsedLists.size()));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private void generateFile(Object object, String fileName, OutputStream outputStream) throws IOException {
        File tempFile = fileExport.exportTxt(fileName, object).toFile();
        FileInputStream fileInputStream = new FileInputStream(tempFile);
        IOUtils.copy(fileInputStream, outputStream);
        outputStream.close();
        fileInputStream.close();
        logger.info(String.format("File '%s' %sdeleted from server.", fileName, tempFile.delete() ? "" : "not "));
    }


    private boolean validateHeader(String hash) {
        if (userHash == null) userHash = userRepository.findActiveTokens();
        return !userHash.contains(hash);
    }

    private boolean hasAccess(String hash, Long listId) {
        return taskListRepository.hasListAccess(hash, listId) != 0;
    }

}
