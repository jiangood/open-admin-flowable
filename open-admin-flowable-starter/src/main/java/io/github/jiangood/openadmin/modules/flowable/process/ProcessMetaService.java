package io.github.jiangood.openadmin.modules.flowable.process;

import io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProcessMetaService {

    private static final String PROCESS_DEFINITION_PATTERN = "classpath*:data/flowable-process-definition*.yml";

    private Map<String, ProcessMeta> cache;

    @PostConstruct
    void initCache() {
        log.info("===== 加载流程元数据缓存 =====");
        cache = loadAll().stream()
                .collect(Collectors.toUnmodifiableMap(ProcessMeta::getKey, Function.identity()));
        log.info("===== 共加载 {} 个流程定义 =====", cache.size());
    }

    private List<ProcessMeta> loadAll() {
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

    public List<ProcessMeta> findAll() {
        return new ArrayList<>(cache.values());
    }

    public ProcessMeta findOne(String key) {
        return cache.get(key);
    }
}
