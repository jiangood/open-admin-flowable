package io.github.jiangood.openadmin.modules.flowable.process;

import io.github.jiangood.openadmin.util.field.ValueType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProcessVariable {
    private String name;
    private String label;
    private ValueType valueType = ValueType.STRING;
    private boolean required = false;

    public ProcessVariable(String name, String label) {
        this.name = name;
        this.label = label;
    }


}
