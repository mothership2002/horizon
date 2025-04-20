package horizon.core.engine;

import horizon.core.command.Command;
import horizon.core.conductor.Conductor;
import horizon.core.constant.Scheme;
import horizon.core.context.HorizonRuntimeUnit;
import horizon.core.context.HorizonSystemContext;
import horizon.core.model.HorizonContext;
import horizon.core.model.RawInput;
import horizon.core.model.RawOutput;
import horizon.core.rendezvous.AbstractRendezvous;
import horizon.core.rendezvous.Rendezvous;
import horizon.core.stage.StageHandler;

import java.util.Optional;

/**
 * HorizonFlowEngine은 하나의 진입점(run)으로
 * • RawInput → HorizonContext 생성(encounter)
 * • 의도 → Conductor → Command → 실행
 * • StageHandler → RawOutput 변환
 * • Rendezvous.fallAway(context) 호출
 * 전체 플로우를 orchestration 합니다.
 */
public class HorizonFlowEngine {
    private final HorizonSystemContext systemContext;

    public HorizonFlowEngine(HorizonSystemContext systemContext) {
        this.systemContext = systemContext;
    }

    public RawOutput run(RawInput input) {
        // 1) 스킴별 RuntimeUnit 조회
        Optional<HorizonRuntimeUnit<RawInput, Object, Object, Object, RawOutput>> optUnit
                = systemContext.resolveUnit(Scheme.valueOf(input.getScheme()));
        HorizonRuntimeUnit<?, ?, ?, ?, ?> unit =
                optUnit.orElseThrow(() -> new IllegalStateException("No runtime for scheme " + input.getScheme()));

        // 2) Rendezvous로 Context 생성
        @SuppressWarnings("unchecked")
        Rendezvous<RawInput, RawOutput> rendezvous =
                (Rendezvous<RawInput, RawOutput>) unit.getRendezvousDescriptor().rendezvous();
        HorizonContext context = rendezvous.encounter(input);

        // 3) Conductor → Command → 실행 결과 기록
        @SuppressWarnings("unchecked")
        Conductor<Object> conductor =
                (Conductor<Object>) unit.getConductor(context.getParsedIntent())
                        .orElseThrow(() -> new IllegalStateException(
                                "No conductor for intent " + context.getParsedIntent()
                        ));

        Command command = conductor.resolve(context.getIntentPayload());
        Object result = command.execute();
        context.setExecutionResult(result);

        StageHandler handler = unit.getCentralStage(command.getKey())
                .orElseThrow(() -> new IllegalStateException("No stage handler for command " + command.getKey()));
        RawOutput output = handler.handle(context);
        context.setRenderedOutput(output);

        RawOutput finalOutput = rendezvous.fallAway(context);
        return finalOutput != null ? finalOutput : output;
    }
}
