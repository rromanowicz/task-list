package ex.rr.tasklist;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserRepositoryTest extends TestCase {

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USER = "test_user";
    private static User user;


    @Test
    public void shouldCreateUser() {
        createUser();
        assertThat(user).isNotNull();
    }

    @Test
    public void shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSizeGreaterThan(1);
    }

    @Test
    public void shouldReturnUserByName() {
        Optional<User> temp_user = userRepository.findByName("user1");
        assertThat(temp_user).isNotNull();
    }

    @Test
    public void shouldReturnUserById() {
        Optional<User> temp_user = userRepository.findById(1L);
        assertThat(temp_user).isNotNull();
    }

    @Test
    public void shouldDeleteUserById() {
        userRepository.deleteById(user.getId());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    public void createUser() {
        User temp_user = User.builder().name(TEST_USER).build().toBuilder().build();
        user = userRepository.saveAndFlush(temp_user);
    }

}
