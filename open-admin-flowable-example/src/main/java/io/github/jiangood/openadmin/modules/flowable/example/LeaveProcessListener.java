package io.github.jiangood.openadmin.modules.flowable.example;

import io.github.jiangood.openadmin.modules.flowable.domain.ProcessListener;
import io.github.jiangood.openadmin.modules.flowable.core.ProcessEventType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LeaveProcessListener implements ProcessListener {

    @Override
    public void onProcessEvent(ProcessEventType type, String initiator, String businessKey, Map<String, Object> variables) {

    }
}
