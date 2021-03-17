package ex.rr.tasklist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

import static org.hibernate.annotations.CascadeType.ALL;
import static org.hibernate.annotations.CascadeType.MERGE;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TaskList {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "task_list_id_sequence")
    @SequenceGenerator(name = "task_list_id_sequence", sequenceName = "TASK_LIST_ID_SEQ")
    private Long id;

    private String listName;
    private String listDescription;

    @Builder.ObtainVia(method = "createdAtChecker")
    private Long createdAt;

    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();

    @OneToMany(targetEntity = Task.class)
    @Cascade(ALL)
    private List<Task> tasks;

    @OneToOne(targetEntity = User.class)
    @Cascade(MERGE)
    private User owner;

    @OneToMany(targetEntity = User.class)
    @Cascade(MERGE)
    private List<User> sharedWith;

    private Long createdAtChecker() {
        return createdAt == null ? System.currentTimeMillis() : createdAt;
    }

}
