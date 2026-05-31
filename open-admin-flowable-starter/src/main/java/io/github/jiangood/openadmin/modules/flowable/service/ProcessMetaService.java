package io.github.jiangood.openadmin.modules.flowable.service;

import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.dao.IProcessMetaDao;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProcessMetaService {

    private final List<IProcessMetaDao> daoList;
    private Map<String, ProcessMeta> cache;

    public ProcessMetaService(List<IProcessMetaDao> daoList) {
        this.daoList = daoList;
    }

    @PostConstruct
    void initCache() {
        log.info("===== 加载流程元数据缓存 =====");
        cache = loadAll().stream()
                .collect(Collectors.toUnmodifiableMap(ProcessMeta::getKey, Function.identity()));
        log.info("===== 共加载 {} 个流程定义 =====", cache.size());
    }

    private List<ProcessMeta> loadAll() {
        List<ProcessMeta> list = new ArrayList<>();
        for (IProcessMetaDao dao : daoList) {
            list.addAll(dao.findProcessMetaList());
        }
        return list;
    }

    public List<ProcessMeta> findAll() {
        return new ArrayList<>(cache.values());
    }

    public ProcessMeta findOne(String key) {
        return cache.get(key);
    }
}
