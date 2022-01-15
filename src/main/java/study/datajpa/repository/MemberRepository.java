package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

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

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    //==다양한 리턴타입==//
    List<Member> findListByUsername(String username); //컬렉션
    Member findMemberByUsername(String username); //단건
    Optional<Member> findOptionalByUsername(String username); //단건 Optional

    //==페이징==//
    Page<Member> findByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);
    List<Member> findListByAge(int age, Pageable pageable);

    //페이징 쿼리와 카운트 쿼리 분리해서 이렇게 할 수도 있다.
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findCountQuerySeparateByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true) //executeUpdate와 같은 역할
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"}) //fetch조인 매번 쿼리적어주기 귀찮으니 이렇게 해주면 fetch join과 같은효과.
    List<Member> findAll();


    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph(); //이런식으로 Query를 직접 짜고 여기에 fetch join만 추가해줄수도 있음. (EntityGraph를 추가한건 결국 fetch join을 추가했다고 보면된다.)

    @EntityGraph(attributePaths = ("team"))
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);

}
