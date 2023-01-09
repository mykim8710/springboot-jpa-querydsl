package com.example.querydsl.controller;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.repository.MemberJpaRepository;
import com.example.querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MemberApiController {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/api/v1/members")
    public List<MemberTeamDto> searchAllMemberV1(MemberSearchCondition condition) {
        log.info("[GET] /api/v1/members  =>  search Member v1, 순수 JPA Repository(Querydsl) 동적쿼리");
        log.info("MemberSearchCondition = {}", condition);
        return memberJpaRepository.searchWhereParameter(condition);
    }

    @GetMapping("/api/v2/members")
    public Page<MemberTeamDto> searchAllMemberV2(MemberSearchCondition condition, Pageable pageable) {
        log.info("[GET] /api/v2/members  =>  search Member v2, Spring Data Repository(Querydsl), 동적쿼리 + 페이징");
        log.info("MemberSearchCondition = {}", condition);
        return memberRepository.searchPaginationSimple(condition, pageable);
    }

    @GetMapping("/api/v3/members")
    public Page<MemberTeamDto> searchAllMemberV3(MemberSearchCondition condition, Pageable pageable) {
        log.info("[GET] /api/v2/members  =>  search Member v3, Spring Data Repository(Querydsl), 동적쿼리 + 페이징");
        log.info("MemberSearchCondition = {}", condition);
        return memberRepository.searchPaginationCountQueryOptimization(condition, pageable);
    }
}
