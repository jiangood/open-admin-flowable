package io.github.jiangood.openadmin.modules.flowable.config;

import io.github.jiangood.openadmin.framework.config.OpenLifecycle;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class FlowableDataInit implements OpenLifecycle {


    private ProcessMetaService processMetaService;

    private ProcessService processService;

    @Override
    public void onDataInit() {
        List<ProcessMeta> list = processMetaService.findAll();
        for (ProcessMeta meta : list) {
            String key = meta.getKey();
            processService.initModel(meta);
            log.info("注册流程定义类 {} {}", key, meta.getClass().getName());
        }
    }




}
