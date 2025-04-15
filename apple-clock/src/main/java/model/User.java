package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "user_info")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @OneToMany(mappedBy = "user")
    private List<WorkType> workTypes;

    @OneToMany(mappedBy = "workType")
    private List<WorkLogs> workLogs;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userName;

    public User(String userName) {
        this.userName = userName;
    }
}
