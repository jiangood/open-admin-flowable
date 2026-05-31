package io.github.jiangood.openadmin.modules.flowable.core;

import lombok.AllArgsConstructor;

import java.util.Arrays;

// 参考 FlowableEngineEventType， 名称保持一致
@AllArgsConstructor
public enum ProcessEventType {

    TASK_ASSIGNED("任务分配"),
    TASK_COMPLETED("任务完成"),


    /**
     * 流程已开始，可以访问所有初始化变量
     * 用途：启动后处理、日志记录、通知、修改业务状态
     */
    PROCESS_STARTED("流程启动"),

    PROCESS_COMPLETED("流程完成"),

    PROCESS_CANCELLED("流程终止");


    final String msg;


    public static ProcessEventType findByName(String name) {
        return Arrays.stream(values())
                .filter(v -> v.name().equals(name))
                .findFirst().orElse(null);
    }

}
