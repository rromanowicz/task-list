package ex.rr.tasklist.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

import static org.hibernate.annotations.CascadeType.ALL;

@SuppressWarnings("unused")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "USER_ID_SEQ")
    private Long id;

    @Column(unique = true)
    private String username;

    @Builder.ObtainVia(method = "passwordChecker")
    private String password;

    @Builder.ObtainVia(method = "createdAtChecker")
    private Long createdAt;

    @Builder.ObtainVia(method = "roleChecker")
    @OneToMany(targetEntity = Role.class)
    @Cascade(ALL)
    private List<Role> roles;

    @OneToMany(targetEntity = HashToken.class)
    @Cascade(ALL)
    private List<HashToken> hashTokens;


    private Long createdAtChecker() {
        return createdAt == null ? System.currentTimeMillis() : createdAt;
    }

    private List<Role> roleChecker() {
        return roles == null || roles.isEmpty() ? initRoles() : roles;
    }

    private List<Role> initRoles() {
        return Collections.singletonList(Role.builder().role("USER").build().toBuilder().build());
    }

    private String passwordChecker() {
        return password == null ? null : BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void initToken(String token) {
        hashTokens = Collections.singletonList(HashToken.builder().token(token).build().toBuilder().build());
    }

}
