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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "work_type_id")
    private WorkType workType;

    private LocalDateTime begin;
    private LocalDateTime end;
    private int duration;

    // 带WorkType的构造器，方便创建新记录
    public WorkLogs(WorkType workType, LocalDateTime begin, LocalDateTime end, int duration) {
        this.workType = workType;
        this.begin = begin;
        this.end = end;
        this.duration = duration;
    }
}
