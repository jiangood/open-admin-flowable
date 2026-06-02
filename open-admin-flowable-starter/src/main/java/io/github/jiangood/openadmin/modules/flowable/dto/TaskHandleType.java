package io.github.jiangood.openadmin.modules.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskHandleType {

    APPROVE("同意"), REJECT("不同意"), BACK("退回");

    final String message;

}
