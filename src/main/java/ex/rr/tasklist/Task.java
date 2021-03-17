package ex.rr.tasklist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_id_sequence")
    @SequenceGenerator(name = "task_id_sequence", sequenceName = "TASK_ID_SEQ")
    private Integer id;
    private String taskName;
    private Boolean completed;
    @Builder.ObtainVia(method = "createdAtChecker")
    private Long createdAt;
    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();
    private Long completedAt;
    private Long dueDate;

    private Long createdAtChecker(){
        return createdAt == null ? System.currentTimeMillis() : createdAt;
    }

}
