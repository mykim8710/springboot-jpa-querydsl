package com.example.querydsl.repository;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberCustom2Repository {
    List<MemberTeamDto> searchWhereParameter2(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPaginationSimple2(MemberSearchCondition condition, Pageable pageable);
}
