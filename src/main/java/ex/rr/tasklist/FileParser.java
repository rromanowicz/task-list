package ex.rr.tasklist;

import com.fasterxml.jackson.databind.ObjectMapper;
import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.response.TaskListResponse;
import ex.rr.tasklist.database.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FileParser {

    @Autowired
    ResponseMapper responseMapper;

    public List<TaskList> toTaskList(MultipartFile file, UserResponse owner) throws IOException {

        if (file.isEmpty())
            throw new FileNotFoundException("File not found.");
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".txt"))
            throw new UnsupportedOperationException("Invalid file extension.");

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        List<TaskListResponse> parsedLists = mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, TaskListResponse.class));

        if (parsedLists.isEmpty())
            throw new UnsupportedOperationException("Invalid file contents.");

        for (TaskListResponse tlr : parsedLists)
            if (tlr.getListName() == null || tlr.getTasks() == null) {
                throw new UnsupportedOperationException("Invalid file contents.");
            } else {
                tlr.setOwner(owner);
            }

        return parsedLists.stream().map(taskListResponse -> responseMapper.mapTaskList(taskListResponse).toBuilder().build()).collect(Collectors.toList());
    }

}
