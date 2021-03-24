package ex.rr.tasklist.database.response;

import ex.rr.tasklist.database.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskListResponse {

    private Long id;
    private String listName;
    private String listDescription;
    private Long createdAt;
    private Long updatedAt;
    private List<Task> tasks;
    private UserResponse owner;
    private List<UserResponse> sharedWith;

}
