package io.github.jiangood.openadmin.modules.flowable.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "flowable")
@Data
public class FlowableProperties {

    /**
     * 审核时，点击不同意（拒绝）后，流程的处理方式
     * 1. 终止流程,删除流程
     * 2. 回滚到上一个节点
     */
    private RejectType rejectType = RejectType.DELETE; // close , rollback


    public enum RejectType {
        DELETE,
        MOVE_BACK
    }
}
