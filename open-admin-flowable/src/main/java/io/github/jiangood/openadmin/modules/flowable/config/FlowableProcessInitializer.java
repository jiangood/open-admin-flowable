package io.github.jiangood.openadmin.modules.flowable.config;

import io.github.jiangood.openadmin.framework.config.StartupHook;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FlowableProcessInitializer implements StartupHook {

    private ProcessMetaService processMetaService;

    private ProcessModelService processModelService;

    @Autowired
    public void setProcessMetaService(@Lazy ProcessMetaService processMetaService) {
        this.processMetaService = processMetaService;
    }

    @Autowired
    public void setProcessModelService(@Lazy ProcessModelService processModelService) {
        this.processModelService = processModelService;
    }

    @Override
    public void beforeSeedDataInitialize() {
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
