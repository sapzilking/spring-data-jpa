package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static lombok.AccessLevel.*;

@Getter
@Setter
@NoArgsConstructor(access = PROTECTED) // JPA는 기본적으로 기본생성자가 있어야함.(프록시 접근 등의 다양한 이유로..)
@ToString(of = {"id", "username", "age"}) // team처럼 연관관계 필드는 출력하지 말자. 양쪽으로 참조를 하다가 stackoverflow가 발생한다.
@NamedQuery(
        name="Member.findByUsername",
        query="select m from Member m where m.username = :username"
) //NamedQuery는 실무에서 거의 사용하지 않는다. 왜냐하면 Repository에 바로 쿼리를 작성할 수 있기 때문이다.
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;


    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id") // JoinColumn의 이름은 foreign key의 이름이다. 이 부분을 다른 엔티티의 기본키의 이름이라고 생각해서 헤깔렸던것 같다.
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    //==연관관계 편의메서드==//
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
