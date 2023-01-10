package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.example.querydsl.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;

public class MemberCustomRepositoryImpl implements MemberCustomRepository {
    private final JPAQueryFactory queryFactory;

    public MemberCustomRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> searchWhereParameter(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .join(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }


    private BooleanExpression usernameEq(String username) {
        return !StringUtils.hasLength(username) ? null : member.username.eq(username);
    }

    private BooleanExpression teamNameEq(String teamName) {
        return !StringUtils.hasLength(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

    @Override
    public Page<MemberTeamDto> searchPaginationSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory
                                                .select(new QMemberTeamDto(
                                                        member.id.as("memberId"),
                                                        member.username,
                                                        member.age,
                                                        team.id.as("teamId"),
                                                        team.name.as("teamName")))
                                                .from(member)
                                                .join(member.team, team)
                                                .where(usernameEq(condition.getUsername()),
                                                        teamNameEq(condition.getTeamName()),
                                                        ageGoe(condition.getAgeGoe()),
                                                        ageLoe(condition.getAgeLoe()))
                                                .offset(pageable.getOffset())       // ~ 번째부터 시작할 것인가?
                                                .limit(pageable.getPageSize()) // 한번 조회 시 ~개 가져올 것인가?
                                                .fetchResults();

        // Querydsl이 제공하는 fetchResults() 를 사용하면 내용과 전체 카운트를 한번에 조회할 수 있다.(실제 쿼리는 2번 호출)
        // fetchResult() 는 카운트 쿼리 실행시 필요없는 order by 는 제거한다.

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }


    @Override
    public Page<MemberTeamDto> searchPaginationComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                                            .select(new QMemberTeamDto(
                                                    member.id.as("memberId"),
                                                    member.username,
                                                    member.age,
                                                    team.id.as("teamId"),
                                                    team.name.as("teamName")))
                                            .from(member)
                                            .join(member.team, team)
                                            .where(usernameEq(condition.getUsername()),
                                                    teamNameEq(condition.getTeamName()),
                                                    ageGoe(condition.getAgeGoe()),
                                                    ageLoe(condition.getAgeLoe()))
                                            .offset(pageable.getOffset())       // ~ 번째부터 시작할 것인가?
                                            .limit(pageable.getPageSize()) // 한번 조회 시 ~개 가져올 것인가?
                                            .fetch();
        /* Deprecated
        long total = queryFactory
                    .select(member)
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(usernameEq(condition.getUsername()),
                            teamNameEq(condition.getTeamName()),
                            ageGoe(condition.getAgeGoe()),
                            ageLoe(condition.getAgeLoe()))
                    .fetchCount(); */

        long total = queryFactory
                        .select(member.count())
                        .from(member)
                        .join(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe()))
                        .fetchOne();

        // 전체 카운트를 조회 하는 방법을 최적화 할 수 있으면 이렇게 분리하면 된다.
        // 예를 들어서 전체 카운트를 조회할 때 조인 쿼리를 줄일 수 있다면 상당한 효과가 있다.
        // 코드를 리펙토링해서 내용 쿼리과 전체 카운트 쿼리를 읽기 좋게 분리하면 좋다.

        return new PageImpl<>(content, pageable, total);
    }


    @Override
    public Page<MemberTeamDto> searchPaginationCountQueryOptimization(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                                        .select(new QMemberTeamDto(
                                                member.id.as("memberId"),
                                                member.username,
                                                member.age,
                                                team.id.as("teamId"),
                                                team.name.as("teamName")))
                                        .from(member)
                                        .join(member.team, team)
                                        .where(usernameEq(condition.getUsername()),
                                                teamNameEq(condition.getTeamName()),
                                                ageGoe(condition.getAgeGoe()),
                                                ageLoe(condition.getAgeLoe()))
                                        .offset(pageable.getOffset())       // ~ 번째부터 시작할 것인가?
                                        .limit(pageable.getPageSize()) // 한번 조회 시 ~개 가져올 것인가?
                                        .fetch();

        /* Deprecated
            JPAQuery<Member> countQuery = queryFactory
                                        .select(member)
                                        .from(member)
                                        .leftJoin(member.team, team)
                                        .where(usernameEq(condition.getUsername()),
                                                teamNameEq(condition.getTeamName()),
                                                ageGoe(condition.getAgeGoe()),
                                                ageLoe(condition.getAgeLoe()));*/
        // () -> countQuery.fetchCount();


        JPAQuery<Long> countQuery = queryFactory
                                        .select(member.count())
                                        .from(member)
                                        .join(member.team, team)
                                        .where(usernameEq(condition.getUsername()),
                                                teamNameEq(condition.getTeamName()),
                                                ageGoe(condition.getAgeGoe()),
                                                ageLoe(condition.getAgeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }
}
