package model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
@Getter
@Setter
@NoArgsConstructor
public class WorkLogs {
    @ManyToOne
    @JoinColumn(name = "work_type_id")
    private WorkType workType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime begin;
    private LocalDateTime end;
    private int duration;

    public WorkLogs(LocalDateTime begin, LocalDateTime end, int duration) {
        this.begin = begin;
        this.end = end;
        this.duration = duration;
    }
}
