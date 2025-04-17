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

    private Integer themeStyle = (Integer) 0;    // 0=红苹果, 1=绿苹果
}
