# Flow Engine 아키텍처 분석

## 개요

이 문서는 Horizon 프레임워크에서 Flow Engine이 Foyer 내에 위치해야 하는지에 대한 분석과 권장 사항을 제공합니다. 현재 구현에서는 Flow Engine이 Foyer에 주입되어 사용되고 있습니다.

## 현재 아키텍처 분석

### 현재 구조

현재 구현에서는 다음과 같은 관계가 있습니다:

1. **HorizonSystemContext**:
   - 시스템의 중앙 레지스트리 역할
   - 런타임 유닛과 Foyer를 등록하고 관리
   - 스키마별로 컴포넌트를 구성

2. **HorizonFlowEngine**:
   - HorizonSystemContext를 의존성으로 가짐
   - 요청 처리 흐름을 오케스트레이션
   - 런타임 유닛을 해결하고 명령을 실행

3. **ProtocolFoyer**:
   - 프로토콜별 요청 처리의 진입점
   - HorizonFlowEngine을 선택적 필드로 가짐
   - Flow Engine이 설정된 경우 요청 처리를 위임

### 현재 구조의 장점

1. **유연성**:
   - Flow Engine은 선택적 컴포넌트로, Foyer는 Flow Engine 없이도 작동 가능
   - 필요에 따라 Flow Engine을 사용하거나 사용하지 않을 수 있음

2. **단순한 통합**:
   - Foyer에 Flow Engine을 주입하는 것은 간단하고 직관적
   - 기존 코드를 크게 변경하지 않고도 통합 가능

3. **명확한 책임 분리**:
   - Foyer는 프로토콜 처리에 집중
   - Flow Engine은 비즈니스 로직 처리에 집중

### 현재 구조의 단점

1. **의존성 방향**:
   - Foyer가 Flow Engine에 의존하는 것은 계층 구조에 맞지 않을 수 있음
   - 일반적으로 하위 계층이 상위 계층에 의존하는 것은 바람직하지 않음

2. **중복 로직**:
   - Foyer와 Flow Engine 모두 Rendezvous를 사용하여 요청을 처리하는 로직이 있음
   - 이는 코드 중복과 일관성 문제를 야기할 수 있음

3. **확장성 제한**:
   - 현재 구조에서는 Flow Engine을 다른 컴포넌트에서 재사용하기 어려울 수 있음
   - Foyer와 Flow Engine 간의 강한 결합은 독립적인 확장을 제한할 수 있음

## 대안 아키텍처 접근 방식

### 대안 1: Flow Engine을 중앙 컴포넌트로 만들기

이 접근 방식에서는 Flow Engine이 모든 요청 처리의 중앙 컴포넌트가 됩니다:

1. Foyer는 요청을 받아 Flow Engine에 전달
2. Flow Engine은 적절한 런타임 유닛을 찾아 요청 처리
3. Foyer는 Flow Engine의 결과를 프로토콜별 응답으로 변환

장점:
- 명확한 책임 분리
- 중앙 집중식 요청 처리
- 코드 중복 감소

단점:
- Flow Engine에 대한 강한 의존성
- Flow Engine 없이는 Foyer를 사용할 수 없음

### 대안 2: Foyer와 Flow Engine을 분리하고 공통 인터페이스 사용

이 접근 방식에서는 Foyer와 Flow Engine이 독립적으로 작동하지만 공통 인터페이스를 통해 통신합니다:

1. RequestProcessor 인터페이스 정의
2. Foyer와 Flow Engine 모두 이 인터페이스 구현
3. HorizonSystemContext가 적절한 RequestProcessor를 선택

장점:
- 느슨한 결합
- 독립적인 확장 가능
- 테스트 용이성

단점:
- 추가적인 추상화 계층
- 구현 복잡성 증가

### 대안 3: Flow Engine을 HorizonSystemContext의 일부로 만들기

이 접근 방식에서는 Flow Engine이 HorizonSystemContext의 일부가 됩니다:

1. HorizonSystemContext가 Flow Engine 인스턴스를 생성하고 관리
2. Foyer는 HorizonSystemContext를 통해 Flow Engine에 접근
3. 모든 컴포넌트가 HorizonSystemContext를 통해 통신

장점:
- 중앙 집중식 관리
- 명확한 의존성 방향
- 컴포넌트 간 일관된 통신

단점:
- HorizonSystemContext의 책임 증가
- 컴포넌트 간 결합도 증가

## 권장 사항

현재 구조와 대안을 분석한 결과, **대안 3: Flow Engine을 HorizonSystemContext의 일부로 만들기**가 가장 적합한 접근 방식으로 보입니다.

### 권장 변경 사항

1. **HorizonSystemContext 수정**:
   - Flow Engine을 내부 필드로 추가
   - Flow Engine을 생성하고 관리하는 메서드 추가
   - Flow Engine을 가져오는 getter 메서드 추가

2. **ProtocolFoyer 수정**:
   - Flow Engine 필드 제거
   - HorizonSystemContext를 필드로 추가
   - handleMessage 메서드에서 HorizonSystemContext를 통해 Flow Engine에 접근

3. **HttpDemoApplication 수정**:
   - Flow Engine을 직접 생성하지 않고 HorizonSystemContext에서 가져오도록 수정

이러한 변경을 통해 다음과 같은 이점을 얻을 수 있습니다:

- 명확한 의존성 방향: 모든 컴포넌트가 HorizonSystemContext에 의존
- 중앙 집중식 관리: HorizonSystemContext가 모든 컴포넌트를 관리
- 코드 중복 감소: Flow Engine 생성 및 관리 로직이 한 곳에 집중
- 확장성 향상: 다른 컴포넌트에서도 Flow Engine을 쉽게 사용 가능

## 결론

Flow Engine은 Foyer 내부에 있기보다는 HorizonSystemContext의 일부로 존재하는 것이 더 적합합니다. 이는 컴포넌트 간의 명확한 의존성 방향을 제공하고, 중앙 집중식 관리를 가능하게 하며, 코드 중복을 줄이고 확장성을 향상시킵니다. 권장된 변경 사항을 구현하면 Horizon 프레임워크의 아키텍처가 더욱 견고하고 유지보수하기 쉬워질 것입니다.