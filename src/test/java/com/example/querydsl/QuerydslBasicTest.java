package com.example.querydsl;

import com.example.querydsl.dto.MemberAliasDto;
import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.QMemberDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;


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


    /**
     *팀A에 소속된 모든 회원
     */
    @Test
    @DisplayName("querydsl_기본조인_Test")
    void querydsl_기본조인_Test() {
        List<Member> result = queryFactory
                                .selectFrom(member)
                                .join(member.team, team)
                                .where(team.name.eq("TeamA"))
                                .fetch();

        System.out.println("result = " + result);

        Assertions.assertThat(result)
                            .extracting("username")
                            .containsExactly("member1", "member2");
    }


    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    @DisplayName("querydsl_세타조인_Test")
    void querydsl_세타조인_Test() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Member> result = queryFactory
                                .select(member)
                                .from(member, team)
                                .where(member.username.eq(team.name))
                                .fetch();

        System.out.println("result = " + result);

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("TeamA", "TeamB");
    }


    /**
     * 1. 조인 대상 필터링
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'TeamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID = t.id AND t.name='TeamA'
     */
    @Test
    @DisplayName("querydsl_조인_ON절_필터링_Test")
    void querydsl_조인_ON절_필터링_Test() {
        List<Tuple> result = queryFactory
                                .select(member, team)
                                .from(member)
                                .leftJoin(member.team, team).on(team.name.eq("TeamA"))
                                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }


    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name */
    @Test
    @DisplayName("querydsl_조인_관계없는_엔티티_외부조인_Test")
    void querydsl_조인_관계없는_엔티티_외부조인_Test() {
        em.persist(new Member("TeamA"));
        em.persist(new Member("TeamB"));

        List<Tuple> result = queryFactory
                                .select(member, team)
                                .from(member)
                                .leftJoin(team).on(member.username.eq(team.name))
                                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    @DisplayName("querydsl_페치조인_미적용_Test")
    void querydsl_페치조인_미적용_Test() {
        // 영속성 컨텍스트 db반영, 초기화
        em.flush();
        em.clear();

        Member findMember = queryFactory
                                .selectFrom(member)
                                .where(member.username.eq("member1"))
                                .fetchOne();

        //System.out.println(findMember.getTeam().getName()); // 지연로딩

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());// Team의 로딩여부 확인
        System.out.println("loaded = " + loaded);
        Assertions.assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("querydsl_페치조인_적용_Test")
    void querydsl_페치조인_적용_Test() {
        // 영속성 컨텍스트 db반영, 초기화
        em.flush();
        em.clear();

        Member findMember = queryFactory
                                .selectFrom(member)
                                .join(member.team, team).fetchJoin()
                                .where(member.username.eq("member1"))
                                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());// Team의 로딩여부 확인
        System.out.println("loaded = " + loaded);
        Assertions.assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    @DisplayName("querydsl_서브쿼리_eq_Test")
    void querydsl_서브쿼리_eq_Test() {
        QMember memberSub = new QMember("memberSub");   // 서브쿼리용

        List<Member> result = queryFactory
                                    .selectFrom(member)
                                    .where(member.age.eq(
                                            JPAExpressions
                                                    .select(memberSub.age.max())
                                                    .from(memberSub)
                                    ))
                                    .fetch();
        System.out.println("result = " + result);

        Assertions.assertThat(result).extracting("age").containsExactly(40);
    }

    /**
     * 나이가 평균나이 이상인 회원
     */
    @Test
    @DisplayName("querydsl_서브쿼리_goe(greater or equal)_Test")
    void querydsl_서브쿼리_goe_Test() {
        QMember memberSub = new QMember("memberSub");   // 서브쿼리용

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        System.out.println("result = " + result);

        Assertions.assertThat(result).extracting("age").containsExactly(30, 40);
    }


    /**
     * 서브쿼리 여러 건 처리, in 사용
     **/
    @Test
    @DisplayName("querydsl_서브쿼리_in_Test")
    void querydsl_서브쿼리_in_Test() {
        QMember memberSub = new QMember("memberSub");   // 서브쿼리용

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        System.out.println("result = " + result);

        Assertions.assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    /**
     * select 절에 subquery
     **/
    @Test
    @DisplayName("querydsl_서브쿼리_select절_Test")
    void querydsl_서브쿼리_select절_Test() {
        QMember memberSub = new QMember("memberSub");   // 서브쿼리용

        List<Tuple> tuples = queryFactory
                                .select(member.username,
                                        JPAExpressions
                                                .select(memberSub.age.avg())
                                                .from(memberSub)
                                ).from(member)
                                .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("member.username = " + tuple.get(member.username));
            System.out.println("memberSub.age.avg = " +tuple.get(JPAExpressions.select(memberSub.age.avg()).from(memberSub)));
        }
    }

    @Test
    @DisplayName("querydsl_case문_단순한조건_Test")
    void querydsl_case문_단순한조건_Test() {
        List<String> result = queryFactory.select(member.age
                                                    .when(10).then("열살")
                                                    .when(20).then("스무살")
                                                    .when(30).then("서른살")
                                                    .otherwise("마흔살").as("나이한글표기"))
                                            .from(member)
                                            .fetch();
        System.out.println("result = " + result);

        for (String s : result) {
            System.out.println("나이 = " + s);
        }
    }

    @Test
    @DisplayName("querydsl_case문_복잡한조건_Test")
    void querydsl_case문_복잡한조건_Test() {
        List<String> result = queryFactory.select(new CaseBuilder()
                                                    .when(member.age.between(0,20)).then("0 ~ 20살")
                                                    .when(member.age.between(21,30)).then("21 ~ 30살")
                                                    .otherwise("기타"))
                                            .from(member)
                                            .fetch();

        System.out.println("result = " + result);

        for (String s : result) {
            System.out.println("나이 = " + s);
        }
    }

    /**
     * orderBy에서 Case 문 함께 사용하기 예제
     * 예를 들어서 다음과 같은 임의의 순서로 회원을 출력하고 싶다면?
     * 1. 0 ~ 30살이 아닌 회원을 가장 먼저 출력 == 40살을 가장 먼저 출력
     * 2. 0 ~ 20살 회원 출력
     * 3. 21 ~ 30살 회원 출력
     */
    @Test
    @DisplayName("querydsl_case문_orderby_Test")
    void querydsl_case문_orderby_Test() {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                                                    .when(member.age.between(0, 20)).then(2)
                                                    .when(member.age.between(21, 30)).then(1)
                                                    .otherwise(3);

        List<Tuple> result = queryFactory
                                .select(member.username, member.age, rankPath)
                                .from(member)
                                .orderBy(rankPath.desc())   // 내림차순 3,2,1....
                                .fetch();
        System.out.println("result = " + result);

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            Integer rank = tuple.get(rankPath);

            System.out.println("username = " + username + " age = " + age + " rank = " + rank);
        }
    }

    @Test
    @DisplayName("querydsl_상수처리_Test")
    void querydsl_상수처리_Test() {
        Tuple result = queryFactory
                        .select(member.username, Expressions.constant("CONSTANT"))
                        .from(member)
                        .fetchFirst();

        System.out.println("result = " + result);
    }

    @Test
    @DisplayName("querydsl_문자더하기_Test")
    void querydsl_문자더하기_Test() {

        List<String> result = queryFactory
                                .select(member.username.concat("_").concat(member.age.stringValue()))
                                .from(member)
                                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("querydsl_프로젝션결과반환_하나_Test")
    void querydsl_프로젝션결과반환_하나_Test() {
        List<String> result = queryFactory
                                .select(member.username)
                                .from(member)
                                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    @DisplayName("querydsl_프로젝션결과반환_여러개_tuple_Test")
    void querydsl_프로젝션결과반환_여러개_tuple_Test() {
        List<Tuple> result = queryFactory
                                .select(member.username, member.age)
                                .from(member)
                                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);

            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    @Test
    @DisplayName("querydsl_프로젝션결과반환_순수jpa_dto반환_Test")
    void querydsl_프로젝션결과반환_순수jpa_dto반환_Test() {
        String jpql = "select new com.example.querydsl.dto.MemberDto(m.username, m.age) from Member m";
        List<MemberDto> resultList = em.createQuery(jpql, MemberDto.class).getResultList();

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getAge() = " + memberDto.getAge());
        }
    }

    // dto getter, setter, default constructor 필요
    @Test
    @DisplayName("querydsl_프로젝션결과반환_querydsl빈생성_프로퍼티접근_Test")
    void querydsl_프로젝션결과반환_querydsl빈생성_프로퍼티접근_Test() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                                member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getAge() = " + memberDto.getAge());
        }
    }

    // dto getter, setter 없어도 됨
    @Test
    @DisplayName("querydsl_프로젝션결과반환_querydsl빈생성_필드직접접근 _Test")
    void querydsl_프로젝션결과반환_querydsl빈생성_필드직접접근() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getAge() = " + memberDto.getAge());
        }
    }

    @Test
    @DisplayName("querydsl_프로젝션결과반환_querydsl빈생성_생성자사용_Test")
    void querydsl_프로젝션결과반환_querydsl빈생성_생성자사용_Test() {
        List<MemberDto> result = queryFactory
                                    .select(Projections.constructor(MemberDto.class,
                                            member.username,
                                                    member.age))
                                    .from(member)
                                    .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getAge() = " + memberDto.getAge());
        }
    }


    @Test
    @DisplayName("querydsl_프로젝션결과반환_별칭이다를때_Test")
    void querydsl_프로젝션결과반환_별칭이다를때_Test() {
        List<MemberAliasDto> result = queryFactory
                                        .select(Projections.fields(MemberAliasDto.class,
                                                member.username.as("name"),
                                                        ExpressionUtils.as(member.age, "age")))
                                        .from(member)
                                        .fetch();

        for (MemberAliasDto memberAliasDto : result) {
            System.out.println("memberAliasDto.getName() = " + memberAliasDto.getName());
            System.out.println("memberAliasDto.getAge() = " + memberAliasDto.getAge());
        }
    }


    @Test
    @DisplayName("querydsl_프로젝션결과반환_QueryProjection_Test")
    void querydsl_프로젝션결과반환_QueryProjection_Test() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto.getUsername() = " + memberDto.getUsername());
            System.out.println("memberDto.getAge() = " + memberDto.getAge());
        }
    }

    @Test
    @DisplayName("querydsl_동적쿼리_BooleanBuilder_Test")
    void querydsl_동적쿼리_BooleanBuilder_Test() {
        // given
        String usernameParameter = "member1";
        int ageParameter = 10;

        // when
        List<Member> members = searchMember1(usernameParameter, ageParameter);
        System.out.println("members = " + members);

        // then
        Assertions.assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParameter, Integer ageParameter) {
        BooleanBuilder builder = new BooleanBuilder();

        if(usernameParameter != null) {
            builder.and(member.username.eq(usernameParameter));
        }

        if(ageParameter != null) {
            builder.and(member.age.eq(ageParameter));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    @DisplayName("querydsl_동적쿼리_Where다중파라미터_Test")
    void querydsl_동적쿼리_Where다중파라미터_Test() {
        // given
        String usernameParameter = "member1";
        int ageParameter = 10;

        // when
        List<Member> members = searchMember2(usernameParameter, ageParameter);
        System.out.println("members = " + members);

        // then
        Assertions.assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParameter, int ageParameter) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameParameter), ageEq(ageParameter))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParameter) {
        return usernameParameter != null ? member.username.eq(usernameParameter) : null;
    }
    private BooleanExpression ageEq(Integer ageParameter) {
        return ageParameter != null ? member.age.eq(ageParameter) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }


    /**
     * 비교 표현식 줄임말
     * eq - equal ( = )
     * ne - not equal ( <> )
     * lt - little ( < )
     * le - little or equal ( <= )
     * gt - greater ( > )
     * ge - greater or equal ( >= )
     */
    @Test
    @DisplayName("querydsl_수정_벌크연산_Test")
    void querydsl_수정_벌크연산_Test() {
        long count = queryFactory
                        .update(member)
                        .set(member.username, "이름_수정")
                        .where(member.age.lt(28)) // where age < 28
                        .execute();

        System.out.println("count = " + count);
    }

    @Test
    @DisplayName("querydsl_삭제_벌크연산_Test")
    void querydsl_삭제_벌크연산_Test() {
        long count = queryFactory
                        .delete(member)
                        .where(member.age.gt(18)) // where age > 18
                        .execute();

        System.out.println("count = " + count);
    }

    /**
     * member → M으로 변경하는 replace 함수 사용
     */
    @Test
    @DisplayName("querydsl_SQL_function_호출1_Test")
    void querydsl_SQL_function_호출1_Test() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    /**
     * 소문자로 변경해서 비교
     */
    @Test
    @DisplayName("querydsl_SQL_function_호출2_Test")
    void querydsl_SQL_function_호출2_Test() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }




}
