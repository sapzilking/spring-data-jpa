package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.domain.Sort.Direction.*;

@Rollback(false)
@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired EntityManager em; //같은 트랜잭션 안에서는 다 같은 EntityManager를 사용한다. 즉 memberRepository와 teamRepository는 같은 em을 사용한다.

    @Test
    public void testMember() throws Exception {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() throws Exception {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUserNameAndAgeGreaterThen() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);
    }

    @Test
    public void testQuery() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() throws Exception {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }


    @Test
    public void findByNames() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() throws Exception {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> aaa = memberRepository.findListByUsername("CCC"); //리스트 조회는 결과가 없어도 Empty리스트가 반환되므로 null이 아니다.
        Member findMember = memberRepository.findMemberByUsername("CCC"); //단건조회는 결과가 없으면 null이 반환된다.
        System.out.println("findMember = " + findMember);
        System.out.println("aaa.size = " + aaa.size());
        Optional<Member> optionalByUsername = memberRepository.findOptionalByUsername("CCC"); //단건조회에서 null이 발생하는경우는 사실 Optional로 감싸면 해결된다.
        System.out.println("optionalByUsername = " + optionalByUsername);
    }

    @Test
    public void paging() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, DESC, "username");

        //when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        //Dto로 변환하는 예제
        //이런식으로 Entity를 절대 반환하지 말고 Dto로 변환해서 반환해야 한다.
        Page<MemberDto> toMap = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));


        //then
        List<Member> content = page.getContent(); //page내의 내부 content가져오기
        long totalElements = page.getTotalElements(); //totalCount랑 같음

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void paging_slice() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, DESC, "username");

        //when
        Slice<Member> page = memberRepository.findSliceByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent(); //page내의 내부 content가져오기

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    //그냥 데이터만 조회
    @Test
    public void paging_list() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, DESC, "username");

        //when
        List<Member> page = memberRepository.findListByAge(age, pageRequest);
    }

    /**
     * count쿼리 분리 테스트
     * 실무에서는 count쿼리가 실제 데이터를 가져오는 쿼리와 다르게 더 간단하게 짤 수 있는 경우가 있는데 이럴 때 성능최적화를 위해서 count쿼리를 따로 분리해서 사용한다.
     * */
    @Test
    public void count쿼리분리테스트() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        //sort조건이 복잡해 지면 아래와 같이 풀 수 없는 경우가 있다. 그럴 땐 직접 repository에 query에 sort조건을 적어주자.
        PageRequest pageRequest = PageRequest.of(0, 3, DESC, "username");

        //when
        Page<Member> page = memberRepository.findCountQuerySeparateByAge(age, pageRequest);

        //then
        List<Member> content = page.getContent(); //page내의 내부 content가져오기
        long totalElements = page.getTotalElements(); //totalCount랑 같음

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    //Spring Data JPA를 이용한 bulkUpdate
        @Test
        public void bulkUpdate() throws Exception {
            //given
            memberRepository.save(new Member("member1", 10));
            memberRepository.save(new Member("member2", 19));
            memberRepository.save(new Member("member3", 20));
            memberRepository.save(new Member("member4", 21));
            memberRepository.save(new Member("member5", 40));

            //when
            //bulk연산은 영속성 컨텍스트를 거치지 않고 바로 DB에 접근하여 쿼리를 날리기 때문에 벌크 연산 이후에는 영속성 컨텍스트 초기화를 꼭 해주자!
            //bulk연산 후에 로직이 끝나면 상관없지만, 같은 트랜잭션 안에서 또 다른 로직이 있고 값을 참조하면 실제 DB값과 영속성 컨텍스트에 있는 값이 다르므로 큰일난다.
            //여기서 영속성 컨텍스트를 초기화 해주어도 되고, Spring Data JPA에서는 옵션을 지원한다. (@Modifying에 clearAutomatically속성을 true로 해주면 여기서 초기화를 안해줘도 된다.)
            int resultcount = memberRepository.bulkAgePlus(20);
    //        em.flush();
    //        em.clear();

            List<Member> result = memberRepository.findByUsername("member5");
            Member member5 = result.get(0);
            System.out.println("member5 = " + member5);

            //then
            assertThat(resultcount).isEqualTo(3);
        }

        @Test
        public void findMemberLazy() throws Exception {
            //given
            //member1 -> teamA
            //member2 -> teamB

            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            teamRepository.save(teamA);
            teamRepository.save(teamB);

            Member member1 = new Member("member1", 10, teamA);
            Member member2 = new Member("member2", 10, teamB);
            memberRepository.save(member1);
            memberRepository.save(member2);

            em.flush();
            em.clear();

            //when N + 1 (member를 조회해온 쿼리 1번 + 각 멤버에 해당되는 팀을 멤버 수만큼 조회해오므로 여기가 N번) 즉 N+1문제임
            //select Member 1
//            List<Member> members = memberRepository.findAll();
//            List<Member> members = memberRepository.findMemberEntityGraph();
            List<Member> members = memberRepository.findEntityGraphByUsername("member1");

            for (Member member : members) {
                System.out.println("member = " + member.getUsername());
                System.out.println("member.teamClass = " + member.getTeam().getClass());
                System.out.println("member.team = " + member.getTeam().getName());
            }

            //then
         }
         @Test
        public void findMemberFetchJoin() throws Exception {
            //given
            //member1 -> teamA
            //member2 -> teamB

            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            teamRepository.save(teamA);
            teamRepository.save(teamB);

            Member member1 = new Member("member1", 10, teamA);
            Member member2 = new Member("member2", 10, teamB);
            memberRepository.save(member1);
            memberRepository.save(member2);

            em.flush();
            em.clear();

            //when 위에서 발생한 N+1문제를 해결함
            List<Member> members = memberRepository.findMemberFetchJoin();

            for (Member member : members) {
                System.out.println("member = " + member.getUsername());
                System.out.println("member.teamClass = " + member.getTeam().getClass());
                System.out.println("member.team = " + member.getTeam().getName());
            }

            //then
         }

         @Test
         public void queryHint() throws Exception {
             //given
             Member member1 = memberRepository.save(new Member("member1", 10));
             em.flush();
             em.clear();

             //when
             Member findMember = memberRepository.findReadOnlyByUsername("member1");
             findMember.setUsername("member2");

             em.flush();
         }

         @Test
         public void lock() throws Exception {
             //given
             Member member1 = memberRepository.save(new Member("member1", 10));
             em.flush();
             em.clear();

             //when
             List<Member> result = memberRepository.findLockByUsername("member1");
         }



}