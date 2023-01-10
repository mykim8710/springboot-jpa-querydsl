package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.example.querydsl.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
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

public class MemberCustom2RepositoryImpl extends QuerydslRepositorySupport implements MemberCustom2Repository {

    public MemberCustom2RepositoryImpl() {
        super(Member.class);
    }

    @Override
    public List<MemberTeamDto> searchWhereParameter2(MemberSearchCondition condition) {
        return from(member)
               .join(member.team, team)
               .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
               .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
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
    public Page<MemberTeamDto> searchPaginationSimple2(MemberSearchCondition condition, Pageable pageable) {
        JPQLQuery<MemberTeamDto> jpqlQuery = from(member)
                                            .join(member.team, team)
                                            .where(usernameEq(condition.getUsername()),
                                                    teamNameEq(condition.getTeamName()),
                                                    ageGoe(condition.getAgeGoe()),
                                                    ageLoe(condition.getAgeLoe()))
                                            .select(new QMemberTeamDto(
                                                    member.id.as("memberId"),
                                                    member.username,
                                                    member.age,
                                                    team.id.as("teamId"),
                                                    team.name.as("teamName")));


        JPQLQuery<MemberTeamDto> result = getQuerydsl().applyPagination(pageable, jpqlQuery);

        List<MemberTeamDto> content = result.fetch();
        long count = result.fetchCount();
        return new PageImpl<>(content, pageable, count);
    }
}
