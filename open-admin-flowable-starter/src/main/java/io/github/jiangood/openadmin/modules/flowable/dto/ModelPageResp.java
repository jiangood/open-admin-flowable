package io.github.jiangood.openadmin.modules.flowable.dto;

import org.flowable.engine.repository.Model;

import java.util.Date;

public record ModelPageResp(
        String id,
        String name,
        String key,
        String category,
        Date createTime,
        Date lastUpdateTime,
        int version,
        String metaInfo,
        String deploymentId,
        String tenantId,
        boolean hasEditorSource,
        boolean hasEditorSourceExtra
) {
    public static ModelPageResp from(Model m) {
        return new ModelPageResp(
                m.getId(), m.getName(), m.getKey(), m.getCategory(),
                m.getCreateTime(), m.getLastUpdateTime(), m.getVersion(),
                m.getMetaInfo(), m.getDeploymentId(), m.getTenantId(),
                m.hasEditorSource(), m.hasEditorSourceExtra()
        );
    }
}
