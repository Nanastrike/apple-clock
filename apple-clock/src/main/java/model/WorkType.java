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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String color; // 颜色代码

    @OneToMany(mappedBy = "workType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkLogs> workLogs;

    public WorkType(String color, String name) {
        this.color = color;
        this.name = name;
    }
}
