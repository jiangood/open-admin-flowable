package io.github.jiangood.openadmin.modules.flowable.dao.impl;

import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.dao.IProcessMetaDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ProcessMetaDaoYmlImpl implements IProcessMetaDao {

    public static final String PROCESS_DEFINITION_PATTERN = "classpath*:data/flowable-process-definition*.yml";

    @Override
    public List<ProcessMeta> findProcessMetaList() {
        List<ProcessMeta> all = new ArrayList<>();
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            var resources = resolver.getResources(PROCESS_DEFINITION_PATTERN);
            if (resources.length == 0) {
                throw new IllegalStateException("未找到 flowable-process-definition*.yml");
            }

            var yamlLoader = new YamlPropertySourceLoader();
            for (var resource : resources) {
                log.info("===== 加载流程定义: {} =====", resource.getFilename());
                var propertySources = yamlLoader.load(resource.getFilename(), resource);
                if (propertySources.isEmpty()) {
                    continue;
                }
                var sources = ConfigurationPropertySources.from(propertySources);
                var binder = new Binder(sources);
                var list = binder.bind("definitions", Bindable.listOf(ProcessMeta.class))
                        .orElseThrow(() -> new IllegalStateException(
                                "请在 " + resource.getFilename() + " 中使用 'definitions:' 替代旧的 'process.list:' 格式"));
                all.addAll(list);
            }

            log.info("===== 共加载 {} 个流程定义 =====", all.size());
            return all;
        } catch (Exception e) {
            throw new RuntimeException("读取流程定义失败: " + e.getMessage(), e);
        }
    }

}