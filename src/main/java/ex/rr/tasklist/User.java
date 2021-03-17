package ex.rr.tasklist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
    private String name;

    @Builder.ObtainVia(method = "createdAtChecker")
    private Long createdAt;

    private Long createdAtChecker() {
        return createdAt == null ? System.currentTimeMillis() : createdAt;
    }

}
