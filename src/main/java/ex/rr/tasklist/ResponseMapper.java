package ex.rr.tasklist;

import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.entity.User;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.UserRepository;
import ex.rr.tasklist.database.response.TaskListResponse;
import ex.rr.tasklist.database.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResponseMapper {

    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private UserRepository userRepository;

    public TaskListResponse mapTaskListResponse(TaskList taskList) {

        return TaskListResponse.builder()
                .id(taskList.getId())
                .listName(taskList.getListName())
                .listDescription(taskList.getListDescription())
                .createdAt(taskList.getCreatedAt())
                .updatedAt(taskList.getUpdatedAt())
                .tasks(taskList.getTasks())
                .owner(mapUserResponse(taskList.getOwner()))
                .sharedWith(mapUserResponseList(taskList.getSharedWith()))
                .build();
    }

    public TaskList mapTaskList(TaskListResponse taskListResponse) {
        UserResponse owner = taskListResponse.getOwner();
        List<UserResponse> sharedWith = taskListResponse.getSharedWith();
        List<User> users = mapUserList(owner, sharedWith);

        return TaskList.builder()
                .id(taskListResponse.getId())
                .listName(taskListResponse.getListName())
                .listDescription(taskListResponse.getListDescription())
                .createdAt(taskListResponse.getCreatedAt())
                .updatedAt(taskListResponse.getUpdatedAt())
                .tasks(taskListResponse.getTasks())
                .owner(owner == null ? null : users.stream().filter(user ->
                        user.getUsername().equals(taskListResponse.getOwner().getUsername())).collect(Collectors.toList()).get(0))
                .sharedWith(sharedWith == null || sharedWith.isEmpty() ? null :
                        users.stream().filter(user -> !user.getUsername().equals(taskListResponse.getOwner().getUsername())).collect(Collectors.toList()))
                .build();
    }

    public UserResponse mapUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }

    private List<UserResponse> mapUserResponseList(List<User> users) {
        List<UserResponse> userList = new ArrayList<>();
        users.forEach(u -> userList.add(mapUserResponse(u)));
        return userList;
    }

    private List<User> mapUserList(UserResponse owner, List<UserResponse> users) {
        if(users != null) {
            users.add(owner);
        } else {
            users = Collections.singletonList(owner);
        }
        return userRepository.findByUsernames(users.stream().map(UserResponse::getUsername).collect(Collectors.toList()));
    }


}
