package ex.rr.tasklist;

import ex.rr.tasklist.database.entity.TaskList;
import ex.rr.tasklist.database.repository.TaskListRepository;
import ex.rr.tasklist.database.repository.TaskRepository;
import ex.rr.tasklist.database.repository.UserRepository;
import ex.rr.tasklist.files.FileExport;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @Order(1)
    public void shouldCreateTxtFile() throws IOException {

        Optional<TaskList> taskList = taskListRepository.findById(1052L);
        if (taskList.isPresent()) {
            exportedFilePath = fileExport.exportTxt("test.txt", taskList);
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

}
