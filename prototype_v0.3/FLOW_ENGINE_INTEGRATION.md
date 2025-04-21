# 플로우 엔진과 랑데뷰 통합

## 개요

이 문서는 Horizon 프레임워크에서 플로우 엔진(Flow Engine)과 랑데뷰(Rendezvous) 컴포넌트를 통합하는 방법에 대해 설명합니다. 이 통합을 통해 서버가 시작될 때 플로우 엔진을 통해 요청을 처리할 수 있게 됩니다.

## 변경 사항

### 1. ProtocolFoyer 클래스 수정

`ProtocolFoyer` 클래스에 다음과 같은 변경 사항을 적용했습니다:

1. `HorizonFlowEngine` 필드 추가
   ```java
   protected HorizonFlowEngine flowEngine;
   ```

2. Flow Engine을 설정하고 가져오는 메서드 추가
   ```java
   public void setFlowEngine(HorizonFlowEngine flowEngine) {
       this.flowEngine = flowEngine;
       LOGGER.info("Flow Engine set for {} foyer", protocol.getName());
   }
   
   public HorizonFlowEngine getFlowEngine() {
       return flowEngine;
   }
   ```

3. `handleMessage` 메서드 수정하여 Flow Engine 사용
   ```java
   protected R handleMessage(M message, String remoteAddress, Object context) {
       try {
           // Convert the message to raw input
           I input = adapter.convertToInput(message, remoteAddress);
           
           // Check if the request should be allowed
           if (!allow(input)) {
               LOGGER.warn("Request from {} was denied by the foyer", remoteAddress);
               return adapter.createForbiddenResponse(context);
           }
           
           O output;
           
           // If Flow Engine is set, use it to process the request
           if (flowEngine != null) {
               LOGGER.debug("Processing request using Flow Engine");
               output = (O) flowEngine.run(input);
           } else {
               // Otherwise, use the rendezvous directly
               LOGGER.debug("Processing request using Rendezvous directly");
               var horizonContext = rendezvous.encounter(input);
               
               // If the context has a failure cause, return an error response
               if (horizonContext.getFailureCause() != null) {
                   LOGGER.warn("Error processing request: {}", horizonContext.getFailureCause().getMessage());
                   return adapter.createErrorResponse(horizonContext.getFailureCause(), context);
               }
               
               // Get the output from the context
               output = rendezvous.fallAway(horizonContext);
           }
           
           // Convert the output to a protocol-specific response
           return adapter.convertToResponse(output, context);
       } catch (Exception e) {
           LOGGER.error("Error handling message: {}", e.getMessage());
           LOGGER.error("",e);
           return adapter.createErrorResponse(e, context);
       }
   }
   ```

### 2. HttpDemoApplication 클래스 수정

`HttpDemoApplication` 클래스를 수정하여 Flow Engine을 초기화하고 사용하도록 했습니다:

1. 필요한 임포트 추가
   ```java
   import horizon.core.constant.Scheme;
   import horizon.core.context.HorizonRuntimeUnit;
   import horizon.core.context.HorizonSystemContext;
   import horizon.core.engine.HorizonFlowEngine;
   import horizon.core.model.HorizonContext;
   import horizon.core.model.RawOutput;
   import horizon.core.rendezvous.RendezvousDescriptor;
   import horizon.core.stage.StageHandler;
   import java.util.Map;
   ```

2. 시스템 컨텍스트 및 런타임 유닛 생성
   ```java
   // Create the system context
   HorizonSystemContext systemContext = new HorizonSystemContext();
   systemContext.initialize();

   // Create the rendezvous
   SimpleHttpRendezvous rendezvous = new SimpleHttpRendezvous();

   // Create the rendezvous descriptor
   RendezvousDescriptor<SimpleHttpInput, SimpleHttpInput, String, Map<String, Object>, SimpleHttpOutput> descriptor = 
       new RendezvousDescriptor<>(
           Scheme.HTTP.name(),
           rendezvous,
           SimpleHttpInput.class,
           SimpleHttpOutput.class
       );

   // Create the runtime unit
   HorizonRuntimeUnit<SimpleHttpInput, SimpleHttpInput, String, Map<String, Object>, SimpleHttpOutput> runtimeUnit = 
       new HorizonRuntimeUnit<>(descriptor);
   ```

3. 스테이지 핸들러 및 컨덕터 등록
   ```java
   // Create a simple stage handler that returns the rendered output from the context
   StageHandler defaultStageHandler = new StageHandler() {
       @Override
       public RawOutput handle(HorizonContext context) {
           // If the context has a rendered output, return it
           if (context.getRenderedOutput() != null) {
               return context.getRenderedOutput();
           }
           
           // If the context has an execution result, create a SimpleHttpOutput from it
           Object result = context.getExecutionResult();
           if (result != null) {
               return new SimpleHttpOutput(result.toString(), "text/plain");
           }
           
           // If the context has a failure cause, create an error response
           if (context.getFailureCause() != null) {
               return new SimpleHttpOutput("Error: " + context.getFailureCause().getMessage(), 500, "text/plain");
           }
           
           // Default response
           return new SimpleHttpOutput("No content", 204, "text/plain");
       }
   };
   
   // Register the default stage handler
   runtimeUnit.registerCentralStage("default", defaultStageHandler);

   // Register the UserConductor
   UserConductor userConductor = new UserConductor();
   runtimeUnit.registerConductor("users", userConductor);

   // Register the runtime unit with the system context
   systemContext.registerUnit(Scheme.HTTP, runtimeUnit);
   ```

4. Flow Engine 생성 및 Foyer에 설정
   ```java
   // Create the Flow Engine
   HorizonFlowEngine flowEngine = new HorizonFlowEngine(systemContext);

   // Create the foyer with Flow Engine
   NettyHttpFoyer<SimpleHttpInput, SimpleHttpOutput> foyer = 
       new NettyHttpFoyer<>(HTTP_PORT, rendezvous, adapter);
   
   // Set the Flow Engine in the foyer
   foyer.setFlowEngine(flowEngine);
   ```

## 작동 방식

1. 서버가 시작되면 `HorizonSystemContext`가 초기화됩니다.
2. HTTP 스키마에 대한 런타임 유닛이 생성되고 시스템 컨텍스트에 등록됩니다.
3. `HorizonFlowEngine`이 생성되고 시스템 컨텍스트를 사용하여 초기화됩니다.
4. `NettyHttpFoyer`가 생성되고 Flow Engine이 설정됩니다.
5. 요청이 들어오면 `ProtocolFoyer.handleMessage` 메서드가 호출됩니다.
6. Flow Engine이 설정되어 있으면 요청은 Flow Engine을 통해 처리됩니다.
7. Flow Engine은 적절한 런타임 유닛을 찾아 요청을 처리하고 응답을 반환합니다.

## 결론

이 통합을 통해 Horizon 프레임워크는 더 일관된 방식으로 요청을 처리할 수 있게 되었습니다. 모든 요청은 Flow Engine을 통해 처리되므로 중앙 집중식 로깅, 모니터링, 오류 처리 등의 기능을 쉽게 구현할 수 있습니다. 또한 Flow Engine은 런타임 유닛을 통해 컨덕터와 스테이지 핸들러를 관리하므로 코드의 모듈성과 재사용성이 향상됩니다.