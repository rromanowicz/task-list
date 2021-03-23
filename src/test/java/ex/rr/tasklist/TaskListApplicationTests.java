package ex.rr.tasklist;

import ex.rr.tasklist.controller.ApiController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest
class TaskListApplicationTests {

    @Autowired
    private ApiController controller;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

}


