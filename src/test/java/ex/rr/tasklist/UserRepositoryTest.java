package ex.rr.tasklist;

import ex.rr.tasklist.database.entity.HashToken;
import ex.rr.tasklist.database.entity.Role;
import ex.rr.tasklist.database.entity.User;
import ex.rr.tasklist.database.repository.UserRepository;
import junit.framework.TestCase;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest extends TestCase {

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USER = "test_user";
    private static User user;

    @Test
    @Order(1)
    public void injectedComponentsAreNotNull() {
        assertThat(userRepository).isNotNull();
    }

    @Test
    @Order(2)
    public void shouldCreateUser() {
        createUser(TEST_USER);
        assertThat(user).isNotNull();
    }

    @Test
    @Order(3)
    public void shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSizeGreaterThan(1);
    }

    @Test
    @Order(4)
    public void shouldReturnUserByName() {
        Optional<User> temp_user = userRepository.findByUsername("user1");
        assertThat(temp_user).isNotNull();
    }

    @Test
    @Order(5)
    public void shouldReturnUserById() {
        Optional<User> temp_user = userRepository.findById(1L);
        assertThat(temp_user).isNotNull();
    }

    @Test
    @Order(6)
    public void shouldDeleteUserById() {
        userRepository.deleteById(user.getId());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    public void createUser(String username) {
        User temp_user = User.builder()
                .username(username)
                .password("password")
                .hashTokens(Collections.singletonList(HashToken.builder().token("asd").build()))
                .roles(Collections.singletonList(Role.builder().role("USER").build()))
                .build().toBuilder().build();
        user = userRepository.saveAndFlush(temp_user);
    }

}
