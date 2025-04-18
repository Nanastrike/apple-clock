package model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "work_type")
@Getter
@Setter
@NoArgsConstructor
public class WorkType {
    @OneToMany(mappedBy = "workType")
    private List<WorkLogs> workLogs;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private  String color;//color number

    public WorkType(String color, String name) {
        this.color = color;
        this.name = name;
    }
}
