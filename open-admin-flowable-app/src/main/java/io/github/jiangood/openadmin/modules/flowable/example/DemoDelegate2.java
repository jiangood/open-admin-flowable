package io.github.jiangood.openadmin.modules.flowable.example;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Component
public class DemoDelegate2 implements JavaDelegate {


    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("demo-Java处理类2执行....");
    }
}
