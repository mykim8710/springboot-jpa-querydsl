package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberCustomRepository {
    List<MemberTeamDto> searchWhereParameter(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPaginationSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPaginationComplex(MemberSearchCondition condition, Pageable pageable);

    Page<MemberTeamDto> searchPaginationCountQueryOptimization(MemberSearchCondition condition, Pageable pageable);
}
