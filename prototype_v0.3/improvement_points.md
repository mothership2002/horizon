# Horizon Framework 개선 포인트

## 1. 로깅 시스템 개선

### 1.1 로깅 라이브러리 통일
- 기존: 일부 클래스는 `java.util.logging`, 일부 클래스는 `SLF4J`를 사용하여 일관성이 없었음
- 개선: 모든 클래스가 `SLF4J`와 `Logback` 구현체를 사용하도록 통일

### 1.2 로깅 메서드 표준화
- 기존: `LOGGER.fine()`, `LOGGER.warning()`, `LOGGER.log(Level.SEVERE, ...)` 등 다양한 로깅 메서드 사용
- 개선: `LOGGER.debug()`, `LOGGER.warn()`, `LOGGER.error()` 등 SLF4J 표준 메서드로 통일

### 1.3 로깅 포맷 개선
- 기존: 문자열 연결 방식으로 로그 메시지 생성 (예: `"Error: " + e.getMessage()`)
- 개선: 파라미터화된 로깅 방식 사용 (예: `"Error: {}", e.getMessage()`)
  - 이는 로그가 출력되지 않을 때 문자열 연결 비용을 절약함

## 2. 의존성 관리 개선

### 2.1 중앙 집중식 의존성 관리
- 기존: 각 모듈마다 개별적으로 의존성 버전 관리
- 개선: 루트 프로젝트의 `build.gradle.kts`에서 공통 의존성 정의

### 2.2 로깅 의존성 추가
- `org.slf4j:slf4j-api:2.0.9`
- `ch.qos.logback:logback-classic:1.5.16`

## 3. 코드 품질 개선

### 3.1 일관된 코딩 스타일
- 로깅 관련 코드의 일관성 확보
- 모든 클래스에서 동일한 로깅 패턴 사용

### 3.2 에러 처리 개선
- 로깅과 예외 처리를 더 명확하게 분리
- 적절한 로그 레벨 사용 (debug, info, warn, error)

## 4. 향후 개선 가능 사항

### 4.1 로깅 설정 파일 추가
- `logback.xml` 또는 `logback-spring.xml` 파일을 추가하여 로깅 설정 커스터마이징
- 로그 레벨, 출력 형식, 파일 출력 등 설정 가능

### 4.2 MDC(Mapped Diagnostic Context) 활용
- 요청 추적을 위한 트랜잭션 ID 등을 MDC에 저장하여 로그 추적성 향상
- 예: `MDC.put("traceId", context.getTraceId())`

### 4.3 로그 집계 시스템 연동
- ELK 스택(Elasticsearch, Logstash, Kibana) 또는 Graylog 등과 연동
- JSON 형식 로깅 지원 추가

## 5. 변경된 파일 목록

1. `horizon-core/src/main/java/horizon/core/rendezvous/AbstractRendezvous.java`
2. `horizon-core/src/main/java/horizon/core/rendezvous/protocol/ProtocolFoyer.java`
3. `horizon-http/src/main/java/horizon/http/netty/NettyFoyer.java`
4. `horizon-http/src/main/java/horizon/http/netty/NettyHttpFoyer.java`
5. `horizon-http/src/main/java/horizon/http/netty/NettyRequestHandler.java`
6. `build.gradle.kts` (루트 프로젝트)