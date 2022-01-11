package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

//    @Query(name = "Member.findByUsername")  없어도 이름을 보고 Named Query를 찾아옴
    List<Member> findByUsername(@Param("username") String username);

    /**
     * 정적쿼리
     *   - 애플리케이션 로딩 시점에 오류가 뜸. 마치 이름이 없는 Named Query라고 보면 된다.
     *   - 실무에서는 간단한건 이름으로 생성하고 조금 복잡한 정적 쿼리는 아래와 같이 이름을 줄이고 쿼리를 직접 적어주자
     * 동적쿼리는 Querydsl을 사용하자.
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

}
