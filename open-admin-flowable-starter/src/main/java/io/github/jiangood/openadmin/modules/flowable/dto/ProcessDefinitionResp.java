package io.github.jiangood.openadmin.modules.flowable.dto;

import org.flowable.engine.repository.ProcessDefinition;

import java.util.Date;

public record ProcessDefinitionResp(
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
    public static ProcessDefinitionResp from(ProcessDefinition d) {
        return new ProcessDefinitionResp(
                d.getId(), d.getCategory(), d.getName(), d.getKey(),
                d.getDescription(), d.getVersion(), d.getResourceName(),
                d.getDeploymentId(), d.getDiagramResourceName(),
                d.hasStartFormKey(), d.hasGraphicalNotation(), d.isSuspended(),
                d.getTenantId(), d.getDerivedFrom(), d.getDerivedFromRoot(),
                d.getDerivedVersion()
        );
    }
}
