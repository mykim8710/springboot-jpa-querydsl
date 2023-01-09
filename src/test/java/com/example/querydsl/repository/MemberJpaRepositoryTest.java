package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    @DisplayName("순수 JPA repository 테스트")
    void 순수_JPA_repository_테스트() {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when & then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        Assertions.assertThat(result2).containsExactly(member);
    }

    @Test
    @DisplayName("순수 JPA repository Querydsl 테스트")
    void 순수_JPA_repository_Querydsl_테스트() {
        // given
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        // when & then
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_querydsl();
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername_querydsl("member1");
        Assertions.assertThat(result2).containsExactly(member);
    }

    @Test
    @DisplayName("순수 JPA repository Querydsl_동적쿼리 빌더 테스트")
    void 순수_JPA_repository_Querydsl_동적쿼리_빌더_테스트() {
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
        condition.setAgeGoe(35); // >= 35
        condition.setAgeLoe(40); // <= 40
        condition.setTeamName("teamB");

        // when & then
        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        System.out.println("result = " + result);
        Assertions.assertThat(result).extracting("username").containsExactly("member4");

    }


    @Test
    @DisplayName("순수_JPA_repository_Querydsl_동적쿼리_where_parameter_테스트")
    void 순수_JPA_repository_Querydsl_동적쿼리_where_parameter_테스트() {
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
        List<MemberTeamDto> result = memberJpaRepository.searchWhereParameter(condition);
        System.out.println("result = " + result);
        Assertions.assertThat(result).extracting("username").containsExactly("member4");

    }
}