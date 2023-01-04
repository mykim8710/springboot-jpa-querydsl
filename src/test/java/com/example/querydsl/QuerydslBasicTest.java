package com.example.querydsl;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach // test 실행 전 수행
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    @DisplayName("jpql Basic Test")
    void jpqlBasicTest() {
        // member1 조회

        // given
        String jpql = "select m from Member m where m.username =:username";
        String username = "member1";

        // when
        Member findMember = em.createQuery(jpql, Member.class)
                                .setParameter("username", username)
                                .getSingleResult();

        // then
        Assertions.assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("querydsl Basic Test")
    void querydslBasicTest() {
        // member1 조회

        // given
        QMember m = new QMember("m");
        //QMember m = QMember.member;

        String username = "member1";

        // when
        Member findMember = queryFactory
                                .select(m)
                                .from(m)
                                .where(m.username.eq(username))   // 파라미터 바인딩
                                .fetchOne();

        // then
        Assertions.assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("querydsl Q타입 활용 Test")
    void querydsl_Q타입_활용_Test() {
        // member1 조회

        // given
        String username = "member1";

        // when
        Member findMember = queryFactory
                            .select(member) // static import
                            .from(member)
                            .where(member.username.eq(username))   // 파라미터 바인딩
                            .fetchOne();

        // then
        Assertions.assertThat(findMember.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("querydsl 기본검색쿼리 Test")
    void querydsl_기본검색쿼리_test() {
        // given
        String username = "member1";
        int age = 10;

        // when
        Member findMember = queryFactory
                                .selectFrom(member)
                                .where(member.username.eq(username)
                                        .and(member.age.eq(age)))
                                .fetchOne();

        // then
        Assertions.assertThat(findMember.getUsername()).isEqualTo(username);
        Assertions.assertThat(findMember.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("querydsl 결과조회 Test")
    void querydsl_결과조회_Test() {
        // List 조회
        List<Member> members = queryFactory
                                    .selectFrom(member)
                                    .fetch();
        System.out.println("members = " + members);

        // 단건 조회
        Member member = queryFactory
                            .selectFrom(QMember.member)
                            .where(QMember.member.username.eq("member1"))
                            .fetchOne();
        System.out.println("member = " + member);

        // 처음 한 건 조회 Limit 1
        Member limit1Member = queryFactory
                                .selectFrom(QMember.member)
                                .fetchFirst();
        System.out.println("limit1Member = " + limit1Member);

        // 페이징에서 사용
        QueryResults<Member> results = queryFactory
                                                    .selectFrom(QMember.member)
                                                    .fetchResults();

        long total = results.getTotal();
        List<Member> results1 = results.getResults();

        System.out.println("totalCount = " + total);
        System.out.println("members = " + results1);

        // count 쿼리로 변경
        long count = queryFactory
                        .selectFrom(QMember.member)
                        .fetchCount();
        System.out.println("count = " + count);
    }

    /**
     * [회원 정렬 순서]
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     **/
    @Test
    @DisplayName("querydsl_정렬_Test")
    void querydsl_정렬_Test() {
        // given : member 추가
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> findMembers = queryFactory
                                    .selectFrom(member)
                                    .where(member.age.eq(100))
                                    .orderBy(member.age.desc(), member.username.asc().nullsLast())
                                    .fetch();

        Member member5 = findMembers.get(0);
        Member member6 = findMembers.get(1);
        Member memberNull = findMembers.get(2);

        // then
        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    @DisplayName("querydsl_페이징_조회건수제한_Test")
    void querydsl_페이징_조회건수제한_Test() {

        List<Member> findMembers = queryFactory
                                        .selectFrom(member)
                                        .orderBy(member.username.desc())
                                        .offset(1) // 0부터 시작
                                        .limit(2)   // 2페이지에서 2개 가져와라
                                        .fetch();

        for (Member findMember : findMembers) {
            System.out.println("findMember.getUsername() = " + findMember.getUsername());
        }

        Assertions.assertThat(findMembers.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("querydsl_페이징_전체조회수필요_Test")
    void querydsl_페이징_전체조회수필요_Test() {
        QueryResults<Member> results = queryFactory
                                        .selectFrom(member)
                                        .orderBy(member.username.desc())
                                        .offset(1) // 0부터 시작, 2번째 페이지
                                        .limit(2)   // 2개 가져와라
                                        .fetchResults();

        System.out.println("results.getTotal() = " + results.getTotal());
        System.out.println("results.getResults() = " + results.getResults());
        System.out.println("results.getOffset() = " + results.getOffset());
        System.out.println("results.getLimit() = " + results.getLimit());

        List<Member> members = results.getResults();
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
        }

        Assertions.assertThat(results.getTotal()).isEqualTo(4);
        Assertions.assertThat(results.getLimit()).isEqualTo(2);
        Assertions.assertThat(results.getOffset()).isEqualTo(1);
        Assertions.assertThat(results.getResults().size()).isEqualTo(2);
    }


    /**
     * JPQL
     * select
     *      COUNT(m),   // 회원수
     *      SUM(m.age), // 나이 합
     *      AVG(m.age), // 평균 나이
     *      MAX(m.age), // 최대 나이
     *      MIN(m.age)  // 최소 나이
     * from Member m
     */
    @Test
    @DisplayName("querydsl_집합_Test")
    void querydsl_집합_Test() {
        List<Tuple> result = queryFactory
                                .select(member.count(),
                                        member.age.sum(),
                                        member.age.avg(),
                                        member.age.max(),
                                        member.age.min())
                                .from(member)
                                .fetch();

        Tuple tuple = result.get(0);
        System.out.println("tuple = " + tuple);

        Long count = tuple.get(member.count());
        System.out.println("count = " + count);

        Integer sum = tuple.get(member.age.sum());
        System.out.println("sum = " + sum);

        Double avg = tuple.get(member.age.avg());
        System.out.println("avg = " + avg);

        Integer max = tuple.get(member.age.max());
        System.out.println("max = " + max);

        Integer min = tuple.get(member.age.min());
        System.out.println("min = " + min);

        Assertions.assertThat(count).isEqualTo(4);
        Assertions.assertThat(sum).isEqualTo(100);
        Assertions.assertThat(avg).isEqualTo(25);
        Assertions.assertThat(max).isEqualTo(40);
        Assertions.assertThat(min).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    @DisplayName("querydsl_집합_GroupBy_Test")
    void querydsl_집합_GroupBy_Test() {
        List<Tuple> result = queryFactory
                                .select(team.name, member.age.avg())
                                .from(member)
                                .join(member.team, team)
                                .groupBy(team.name)
                                .fetch();

        Tuple teamA = result.get(0);
        System.out.println("teamA = " + teamA);

        Tuple teamB = result.get(1);
        System.out.println("teamB = " + teamB);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("TeamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        Assertions.assertThat(teamB.get(team.name)).isEqualTo("TeamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }


}