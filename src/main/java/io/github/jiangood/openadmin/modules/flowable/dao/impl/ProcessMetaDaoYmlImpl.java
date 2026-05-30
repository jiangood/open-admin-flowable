package io.github.jiangood.openadmin.modules.flowable.dao.impl;

import cn.hutool.core.io.resource.ResourceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiangood.openadmin.modules.flowable.config.ProcessMetaConfiguration;
import io.github.jiangood.openadmin.modules.flowable.config.meta.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.dao.IProcessMetaDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class ProcessMetaDaoYmlImpl implements IProcessMetaDao {

    public static final String PROCESS_YML = "flowable.yml";

    @Override
    public List<ProcessMeta> findProcessMetaList() {
        try {
            InputStream is = ResourceUtil.getStream(PROCESS_YML);
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(is);
            ObjectMapper mapper = new ObjectMapper();
            ProcessMetaConfiguration cfg = mapper.convertValue(root.get("process"), ProcessMetaConfiguration.class);
            return cfg.getList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
