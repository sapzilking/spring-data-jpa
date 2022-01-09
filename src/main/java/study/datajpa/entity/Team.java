package study.datajpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter @Setter
@NoArgsConstructor(access = PROTECTED) // JPA는 기본적으로 기본생성자가 있어야함.(프록시 접근 등의 다양한 이유로..)
@ToString(of = {"id", "name"}) // members처럼 연관관계 필드는 출력하지 말자. 양쪽으로 참조를 하다가 stackoverflow가 발생한다.
@Entity
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();


    public Team(String name) {
        this.name = name;
    }
}
