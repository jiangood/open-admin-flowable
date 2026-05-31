package io.github.jiangood.openadmin.modules.flowable.dto.vo;

import org.flowable.engine.repository.ProcessDefinition;

import java.util.Date;

public record ProcessDefinitionVO(
        String id,
        String category,
        String name,
        String key,
        String description,
        int version,
        String resourceName,
        String deploymentId,
        String diagramResourceName,
        boolean hasStartFormKey,
        boolean hasGraphicalNotation,
        boolean isSuspended,
        String tenantId,
        String derivedFrom,
        String derivedFromRoot,
        int derivedVersion
) {
    public static ProcessDefinitionVO from(ProcessDefinition d) {
        return new ProcessDefinitionVO(
                d.getId(), d.getCategory(), d.getName(), d.getKey(),
                d.getDescription(), d.getVersion(), d.getResourceName(),
                d.getDeploymentId(), d.getDiagramResourceName(),
                d.hasStartFormKey(), d.hasGraphicalNotation(), d.isSuspended(),
                d.getTenantId(), d.getDerivedFrom(), d.getDerivedFromRoot(),
                d.getDerivedVersion()
        );
    }
}
