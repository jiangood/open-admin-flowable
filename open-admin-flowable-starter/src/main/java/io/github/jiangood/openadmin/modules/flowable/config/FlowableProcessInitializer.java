package io.github.jiangood.openadmin.modules.flowable.config;

import io.github.jiangood.openadmin.framework.config.OpenLifecycle;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessModelService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class FlowableProcessInitializer implements OpenLifecycle {

    private ProcessMetaService processMetaService;

    private ProcessModelService processModelService;

    @Override
    public void onDataInit() {
        log.info("===== FlowableProcessInitializer 开始执行 =====");
        List<ProcessMeta> list = processMetaService.findAll();
        log.info("===== 流程定义数量: {} =====", list.size());
        for (ProcessMeta meta : list) {
            String key = meta.getKey();
            log.info("===== 初始化流程模型: key={}, name={} =====", key, meta.getName());
            processModelService.initModel(meta);
            log.info("注册流程定义 {} {}", key, meta.getClass().getName());
        }
        log.info("===== FlowableProcessInitializer 执行完毕 =====");
    }

}
