package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("Spring_Data_JPA_repository_테스트")
    void Spring_Data_JPA_repository_테스트() {
        // given
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        // when & then
        Member findMember = memberRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        Assertions.assertThat(result2).containsExactly(member);
    }


    @Test
    @DisplayName("Spring_Data_JPA_repository_Querydsl_동적쿼리_where_parameter_테스트")
    void Spring_Data_JPA_repository_Querydsl_동적쿼리_where_parameter_테스트() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setUsername("member4");
        condition.setAgeGoe(35); // >= 35
        condition.setAgeLoe(40); // <= 40
        condition.setTeamName("teamB");

        System.out.println("condition = " + condition);

        // when & then
        List<MemberTeamDto> result = memberRepository.searchWhereParameter(condition);
        System.out.println("result = " + result);
        Assertions.assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    @DisplayName("Spring_Data_JPA_repository_Querydsl_pagination_simple_테스트")
    void Spring_Data_JPA_repository_Querydsl_pagination_simple_테스트() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        // when & then
        Page<MemberTeamDto> result = memberRepository.searchPaginationSimple(condition, pageRequest);
        Assertions.assertThat(result.getSize()).isEqualTo(3);
        Assertions.assertThat(result).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    @DisplayName("Spring_Data_JPA_repository_Querydsl_pagination_complex_테스트")
    void Spring_Data_JPA_repository_Querydsl_pagination_complex_테스트() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        // when & then
        Page<MemberTeamDto> result = memberRepository.searchPaginationComplex(condition, pageRequest);
        Assertions.assertThat(result.getSize()).isEqualTo(3);
        Assertions.assertThat(result).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    @DisplayName("Spring_Data_JPA_repository_Querydsl_pagination_count쿼리_최적화_테스트")
    void Spring_Data_JPA_repository_Querydsl_pagination_count쿼리_최적화_테스트() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        // when & then
        Page<MemberTeamDto> result = memberRepository.searchPaginationComplex(condition, pageRequest);
        Assertions.assertThat(result.getSize()).isEqualTo(3);
        Assertions.assertThat(result).extracting("username").containsExactly("member1", "member2", "member3");
    }

    @Test
    @DisplayName("querydsl_QuerydslPredicateExecutor_테스트")
    void querydsl_QuerydslPredicateExecutor_테스트() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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


        // when & then
        Iterable<Member> result = memberRepository.findAll(
                                        member.age.between(10, 40)
                                                .and(member.username.eq("member1")));
        System.out.println("result = " + result);

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }


}