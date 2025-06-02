# gRPC 스트리밍과 옵저버 패턴 분석

## 개요

이 문서는 gRPC의 스트리밍 방식과 Spring에서의 옵저버 패턴 구현에 대해 분석하고, 이 접근 방식이 현재 프로젝트와 어떻게 어울릴 수 있는지 평가합니다.

## gRPC 스트리밍 기본 개념

gRPC는 Google에서 개발한 고성능, 오픈 소스 RPC(Remote Procedure Call) 프레임워크입니다. 이 프레임워크는 Protocol Buffers를 사용하여 데이터를 직렬화하고, HTTP/2를 기반으로 통신합니다. gRPC의 주요 특징 중 하나는 다양한 스트리밍 모드를 지원한다는 것입니다.

### gRPC의 네 가지 통신 모드

1. **단일 요청-응답 (Unary RPC)**
   - 클라이언트가 서버에 단일 요청을 보내고 단일 응답을 받는 전통적인 RPC 모델
   - 예: `rpc GetUser(GetUserRequest) returns (GetUserResponse);`

2. **서버 스트리밍 (Server Streaming RPC)**
   - 클라이언트가 단일 요청을 보내고 서버로부터 일련의 응답 메시지를 받음
   - 예: `rpc ListUsers(ListUsersRequest) returns (stream UserResponse);`

3. **클라이언트 스트리밍 (Client Streaming RPC)**
   - 클라이언트가 일련의 메시지를 서버에 보내고 단일 응답을 받음
   - 예: `rpc CreateUsers(stream CreateUserRequest) returns (CreateUsersResponse);`

4. **양방향 스트리밍 (Bidirectional Streaming RPC)**
   - 클라이언트와 서버가 독립적으로 메시지 스트림을 주고받음
   - 예: `rpc Chat(stream ChatMessage) returns (stream ChatMessage);`

스트리밍 모드는 대용량 데이터 전송, 실시간 업데이트, 장기 실행 작업 등에 유용합니다.

## Spring에서의 gRPC 스트리밍과 옵저버 패턴

Spring에서 gRPC 스트리밍을 구현할 때 옵저버 패턴이 자연스럽게 적용됩니다. 옵저버 패턴은 객체 간의 일대다 종속성을 정의하여 한 객체의 상태가 변경되면 모든 종속 객체에 자동으로 알림이 가도록 하는 디자인 패턴입니다.

### Spring에서의 구현 방식

Spring에서 gRPC 스트리밍은 주로 다음과 같은 방식으로 구현됩니다:

1. **StreamObserver 인터페이스 사용**
   ```java
   public void streamData(Request request, StreamObserver<Response> responseObserver) {
       // 데이터 처리
       responseObserver.onNext(response1);
       responseObserver.onNext(response2);
       // 스트림 완료
       responseObserver.onCompleted();
   }
   ```

2. **Reactive Streams API와 통합**
   ```java
   public Flux<Response> streamData(Request request) {
       return Flux.interval(Duration.ofSeconds(1))
           .map(i -> Response.newBuilder().setData("Data " + i).build())
           .take(10);
   }
   ```

### 옵저버 패턴의 적용

gRPC 스트리밍에서 옵저버 패턴은 다음과 같이 적용됩니다:

1. **주체(Subject)**: gRPC 서비스 구현체
2. **옵저버(Observer)**: StreamObserver 인터페이스 구현체
3. **이벤트**: 스트림으로 전송되는 각 메시지

StreamObserver 인터페이스는 다음 세 가지 주요 메서드를 제공합니다:
- `onNext(T value)`: 새 메시지가 도착했을 때 호출
- `onError(Throwable t)`: 오류가 발생했을 때 호출
- `onCompleted()`: 스트림이 성공적으로 완료되었을 때 호출

이 패턴은 비동기 처리와 백프레셔(backpressure) 관리에 효과적입니다.

## 현재 프로젝트와의 호환성 분석

현재 Horizon 프레임워크의 gRPC 구현을 분석한 결과, 다음과 같은 특징을 확인했습니다:

### 현재 구현 상태

1. **단일 요청-응답 중심 설계**
   - 현재 프로젝트의 `user.proto` 파일은 모든 서비스 메서드가 단일 요청-응답 방식으로 정의되어 있습니다.
   - 스트리밍 메서드(`stream` 키워드 사용)가 정의되어 있지 않습니다.

2. **GrpcFoyer 클래스의 제한된 스트리밍 지원**
   - `GrpcFoyer` 클래스는 `ServerCall.Listener<String>` 확장을 통해 기본적인 옵저버 패턴을 구현하고 있습니다.
   - 그러나 현재 구현은 주로 단일 메시지 처리에 초점을 맞추고 있으며, 연속적인 스트림 처리를 위한 로직이 부족합니다.

3. **GrpcProtocolAdapter의 DTO 중심 설계**
   - `GrpcProtocolAdapter` 클래스는 DTO 기반 통신에 중점을 두고 있으며, 스트리밍 처리를 위한 특별한 로직이 없습니다.

### 스트리밍 구현을 위한 과제

1. **Proto 파일 업데이트 필요**
   - 스트리밍 서비스를 지원하려면 `user.proto` 파일에 스트리밍 메서드를 추가해야 합니다.

2. **스트리밍 처리 로직 구현 필요**
   - `GrpcFoyer` 클래스를 확장하여 연속적인 메시지 스트림을 처리할 수 있도록 해야 합니다.
   - `onMessage()` 메서드가 여러 번 호출될 수 있도록 로직을 수정해야 합니다.

3. **DTO 매핑 메커니즘 확장 필요**
   - 스트림 데이터에 대한 DTO 매핑 메커니즘을 구현해야 합니다.
   - 연속적인 메시지 변환 및 처리를 위한 로직이 필요합니다.

## 옵저버 패턴 적용 권장사항

현재 프로젝트에 gRPC 스트리밍과 옵저버 패턴을 적용하기 위한 권장사항은 다음과 같습니다:

### 1. 점진적 도입 접근법

1. **파일럿 서비스 선택**
   - 스트리밍이 가장 유용할 수 있는 단일 서비스(예: 실시간 알림, 대용량 데이터 전송)를 선택하여 시작합니다.
   - 이 서비스에 대해 proto 파일을 업데이트하고 스트리밍 구현을 테스트합니다.

2. **기존 아키텍처와의 통합**
   - 기존 DTO 매핑 메커니즘을 스트리밍 컨텍스트로 확장합니다.
   - 스트리밍 처리를 위한 새로운 추상화 계층을 도입합니다.

### 2. 스트리밍 지원을 위한 인프라 개선

1. **StreamObserver 래퍼 구현**
   ```java
   public class HorizonStreamObserver<T> implements StreamObserver<T> {
       private final Consumer<T> onNextHandler;
       private final Consumer<Throwable> onErrorHandler;
       private final Runnable onCompletedHandler;
       
       // 생성자 및 메서드 구현
   }
   ```

2. **스트리밍 컨덕터 메서드 패턴 정의**
   ```java
   @Intent("stream.data")
   @ProtocolAccess(
       schema = {
           @ProtocolSchema(protocol = "gRPC", value = "UserService/StreamData")
       }
   )
   public void streamData(Request request, HorizonStreamObserver<Response> observer) {
       // 스트리밍 로직 구현
   }
   ```

### 3. 리액티브 프로그래밍 통합 고려

1. **Project Reactor 또는 RxJava 도입**
   - 스트리밍 데이터 처리를 위한 리액티브 프로그래밍 라이브러리 도입을 고려합니다.
   - 이를 통해 백프레셔 관리, 비동기 처리, 스트림 변환 등이 용이해집니다.

2. **리액티브 스트림과 gRPC 스트리밍 브릿지 구현**
   ```java
   public Flux<Response> toFlux(Request request) {
       return Flux.create(sink -> {
           StreamObserver<Response> observer = new StreamObserver<>() {
               @Override
               public void onNext(Response value) {
                   sink.next(value);
               }
               
               @Override
               public void onError(Throwable t) {
                   sink.error(t);
               }
               
               @Override
               public void onCompleted() {
                   sink.complete();
               }
           };
           
           // gRPC 서비스 호출
           service.streamData(request, observer);
       });
   }
   ```

## 결론

gRPC 스트리밍과 옵저버 패턴은 실시간 데이터 처리, 대용량 데이터 전송, 이벤트 기반 시스템 등에 매우 유용한 접근 방식입니다. 현재 Horizon 프레임워크는 기본적인 gRPC 지원을 제공하지만, 스트리밍 기능을 완전히 활용하기 위해서는 추가적인 개발이 필요합니다.

프로젝트의 요구사항에 따라 스트리밍이 필요한 경우, 위에서 제안한 점진적 접근법을 통해 기존 아키텍처를 확장하는 것이 좋습니다. 특히 실시간 업데이트가 필요한 기능이나 대용량 데이터를 처리해야 하는 경우 스트리밍 접근법이 큰 이점을 제공할 수 있습니다.

그러나 단순한 CRUD 작업이나 작은 데이터 세트를 다루는 경우에는 현재의 단일 요청-응답 모델이 더 간단하고 효율적일 수 있습니다. 따라서 각 서비스의 특성과 요구사항을 고려하여 스트리밍 적용 여부를 결정하는 것이 중요합니다.