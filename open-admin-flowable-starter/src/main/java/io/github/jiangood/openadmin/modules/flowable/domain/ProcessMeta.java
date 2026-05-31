package io.github.jiangood.openadmin.modules.flowable.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcessMeta {
    private String key;
    private String name;

    private Class<? extends ProcessListener> listener;

    private List<ProcessVariable> variables = new ArrayList<>();
    private List<FormDefinition> forms = new ArrayList<>();

    /**
     * 全局表单（可选）
     * 为空时使用 流程key
     */
    private String globalFormKey;
}
