# SpringBoot JPA Querydsl Study Project

## Project Spec
- 프로젝트 선택
    - Project: Gradle Project
    - Spring Boot: 2.7.7
    - Language: Java
    - Packaging: Jar
    - Java: 11
- Project Metadata
    - Group: com.example
    - Artifact: querydsl
    - Name: querydsl
    - Package name: com.example.querydsl
- Dependencies: **Spring Web**, **Lombok**, **Spring Data JPA**, **H2 Database**
- DB : H2 database

- [Study 내용] 

- Querydsl 기본문법
  - JPQL Basic vs Querydsl Basic
  - 기본 Q-Type 활용
  - 검색 조건 쿼리
  - 결과 조회
  - 정렬
  - 페이징
  - 집합(sum, max, min, count, group by, having....)
  - 조인
    - 기본 조인
    - on절
    - 페치 조인
  - 서브 쿼리
  - Case 문
  - 상수, 문자 더하기
  - distinct

- Querydsl 중급문법
  - 프로젝션과 결과반환 
    - 기본
    - DTO 조회
      - 프로퍼티(setter) 접근, 필드 직접 접근, 생성자 사용
    - @QueryProjection : 생성자 + @QueryProjection
  - 동적 쿼리
    - BooleanBuilder
    - Where 다중 파라미터
  - 수정, 삭제 벌크 연산
  - SQL function 호출

- 실무 활용 : 순수 JPA와 Querydsl
  - 순수 JPA Repository, Querydsl
  - 동적 쿼리와 성능 최적화 조회 : Builder 사용 
  - 동적 쿼리와 성능 최적화 조회 : Where절 파라미터 사용

- 실무 활용 : 스프링 데이터 JPA와 Querydsl
  - 스프링 데이터 JPA Repository
  - 사용자 정의 Repository
  - 스프링 데이터 페이징 활용
    - Querydsl 페이징 연동
    - CountQuery 최적화

- 스프링 데이터 JPA가 제공하는 Querydsl 기능
  - 인터페이스 지원 - QuerydslPredicateExecutor
  - Querydsl Web
  - QuerydslRepositorySupport
  - Querydsl 지원 클래스 직접 정의

- 스프링 부트 2.6 이상, Querydsl 5.0 지원방법
  - 최신 스프링 부트 2.6부터는 Querydsl 5.0을 사용
  - 스프링 부트 2.6 이상 사용시 다음과 같은 부분을 확인 필요
    - 1. `build.gradle` 설정 변경
    - 2. `PageableExecutionUtils` Deprecated(향후 미지원) 패키지 변경
    - 3. `Querydsl fetchResults(), fetchCount()` Deprecated(향후 미지원)