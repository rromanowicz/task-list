package ex.rr.tasklist.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@SuppressWarnings("unused")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder(toBuilder = true)
public class HashToken {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_id_sequence")
    @SequenceGenerator(name = "token_id_sequence", sequenceName = "TOKEN_ID_SEQ")
    private Long id;

    private String token;

    @Builder.ObtainVia(method = "createdAtChecker")
    private Long createdAt;

    @Builder.ObtainVia(method = "activeChecker")
    private Boolean active;

    private Long createdAtChecker() {
        return createdAt == null ? System.currentTimeMillis() : createdAt;
    }

    private boolean activeChecker() {
        return active == null || active;
    }
}
