package model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "misc")
@Getter
@Setter
@NoArgsConstructor
public class Misc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 默认主题风格：0 = 红苹果，1 = 绿苹果
    @Column(nullable = false)
    private Integer themeStyle = 0;

    // 默认语言：0 = English，1 = Mandarin
    @Column(nullable = false)
    private Integer language = 0;

    @Column(nullable = false)
    private String username = "User"; // 默认用户名，初始叫 User
}
