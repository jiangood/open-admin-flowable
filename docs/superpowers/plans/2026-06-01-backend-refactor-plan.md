# Backend Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize backend package structure from flat layered (controller/service/dto/domain) to module-based packaging, split ModelController, and separate domain models from runtime DTOs.

**Architecture:** Flat per-module packaging under `io.github.jiangood.openadmin.modules.flowable`, with 7 business modules (model/process/task/monitor/simulate/diagram) + shared common/ + infrastructure (config/listener/enums/constant). No behavioral changes — pure code moves and splits.

**Tech Stack:** Java 21, Spring Boot, Flowable 8.0.0, Maven

---

## File Structure

### New files to create:
- `model/ModelService.java` — extracted from ModelController (model CRUD + deploy)
- `model/ModelOptionsService.java` — extracted from ModelController (designer options)
- `model/ModelRequest.java` — extracted inner record from ModelController
- `monitor/SetAssigneeRequest.java` — extracted inner record from MonitorController

### Files to move (package + location change):
- `root/FlowableTemplate.java` → `process/`
- `domain/ProcessMeta.java` → `process/`
- `domain/ProcessVariable.java` → `process/`
- `domain/FormDefinition.java` → `process/`
- `service/ProcessMetaService.java` → `process/`
- `service/ProcessService.java` → `process/`
- `service/ProcessModelService.java` → `model/`
- `controller/ModelController.java` → `model/` (also thinned)
- `dto/vo/ModelPageVO.java` → `model/`
- `controller/UserTaskController.java` → `task/`
- `service/UserTaskService.java` → `task/`
- `dto/response/TaskResponse.java` → `task/`
- `dto/response/CommentResponse.java` → `task/`
- `controller/MonitorController.java` → `monitor/`
- `service/MonitorService.java` → `monitor/`
- `dto/response/MonitorTaskResponse.java` → `monitor/`
- `dto/vo/ProcessDefinitionVO.java` → `monitor/`
- `dto/vo/ProcessInstanceVO.java` → `monitor/`
- `controller/SimulateController.java` → `simulate/`
- `service/SimulateService.java` → `simulate/`
- `service/BpmnDiagramService.java` → `diagram/`
- `dto/request/HandleTaskRequest.java` → `common/dto/`
- `dto/TaskHandleType.java` → `common/dto/`
- `utils/FlowablePageTool.java` → `common/utils/`
- `utils/ModelTool.java` → `common/utils/`

### Files to delete (emptied packages):
- `domain/` (empty after moves)
- `dto/request/` (empty after moves)
- `dto/response/` (empty after moves)
- `dto/vo/` (empty after moves)
- `dto/TaskHandleType.java` (moved)
- `root/FlowableTemplate.java` (moved)
- `service/ProcessModelService.java` (moved)
- `service/ProcessMetaService.java` (moved)
- `service/ProcessService.java` (moved)
- `service/UserTaskService.java` (moved)
- `service/MonitorService.java` (moved)
- `service/SimulateService.java` (moved)
- `service/BpmnDiagramService.java` (moved)
- `controller/ModelController.java` (moved + replaced)
- `controller/UserTaskController.java` (moved)
- `controller/MonitorController.java` (moved)
- `controller/SimulateController.java` (moved)
- `utils/FlowablePageTool.java` (moved)
- `utils/ModelTool.java` (moved)

---

### Task 1: Create new package directories + move common/ files

**Files:**
- Create: `src/main/java/.../flowable/common/dto/`
- Create: `src/main/java/.../flowable/common/utils/`
- Create: `src/main/java/.../flowable/process/`
- Create: `src/main/java/.../flowable/model/`
- Create: `src/main/java/.../flowable/task/`
- Create: `src/main/java/.../flowable/monitor/`
- Create: `src/main/java/.../flowable/simulate/`
- Create: `src/main/java/.../flowable/diagram/`
- Move: `dto/TaskHandleType.java` → `common/dto/TaskHandleType.java`
- Move: `dto/request/HandleTaskRequest.java` → `common/dto/HandleTaskRequest.java`
- Move: `utils/FlowablePageTool.java` → `common/utils/FlowablePageTool.java`
- Move: `utils/ModelTool.java` → `common/utils/ModelTool.java`

- [ ] **Step 1: Create new directory structure**

```bash
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/common/dto
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/common/utils
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/process
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/model
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/task
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/monitor
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/simulate
mkdir -p open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/diagram
```

- [ ] **Step 2: Move FlowablePageTool.java — change package to `...flowable.common.utils`**

In `common/utils/FlowablePageTool.java`:
```
package io.github.jiangood.openadmin.modules.flowable.common.utils;
```

- [ ] **Step 3: Move ModelTool.java — change package to `...flowable.common.utils`**

In `common/utils/ModelTool.java`:
```
package io.github.jiangood.openadmin.modules.flowable.common.utils;
```

- [ ] **Step 4: Move TaskHandleType.java — change package to `...flowable.common.dto`**

In `common/dto/TaskHandleType.java`:
```
package io.github.jiangood.openadmin.modules.flowable.common.dto;
```

- [ ] **Step 5: Move HandleTaskRequest.java — change package to `...flowable.common.dto`**

In `common/dto/HandleTaskRequest.java`:
```
package io.github.jiangood.openadmin.modules.flowable.common.dto;
```

- [ ] **Step 6: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/common
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/utils/FlowablePageTool.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/utils/ModelTool.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/TaskHandleType.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/request/HandleTaskRequest.java
git commit -m "refactor: create new package structure, move common utils and DTOs"
```

---

### Task 2: Create ModelService and ModelOptionsService

**Files:**
- Create: `model/ModelService.java`
- Create: `model/ModelOptionsService.java`

Extract business logic from `ModelController.java` into two services:

**ModelService** receives: `RepositoryService`, `ProcessMetaService` — handles:
- `page(searchText, pageable)` — return `Page<ModelPageVO>`
- `detail(id)` — return `Map<String, Object>` with id/name/key/content
- `delete(id)` — call `repositoryService.deleteModel(id)`
- `saveContent(id, content)` — save model editor source
- `deploy(id, xml)` — validate + deploy model (includes `validateModel` via ModelTool)
- `definitionPage(key, pageable)` — query process definitions by key
- `getDefinitionContent(id)` — get BPMN XML

**ModelOptionsService** receives: `SysUserService`, `SysRoleService`, `RepositoryService`, `ProcessMetaService` — handles:
- `javaDelegateOptions()` — list JavaDelegate beans
- `formOptions(code)` — list form definitions for a process
- `assigneeOptions(searchText)` — list users + special assignees
- `candidateGroupsOptions()` — list roles
- `candidateUsersOptions(searchText)` — list users
- `varOptions(code)` — list process variables
- `queryUserOptions(searchText)` — private helper (moved from controller)

- [ ] **Step 1: Create ModelService.java**

```java
package io.github.jiangood.openadmin.modules.flowable.model;

import cn.hutool.core.lang.Dict;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.jiangood.openadmin.modules.flowable.common.utils.FlowablePageTool;
import static io.github.jiangood.openadmin.modules.flowable.common.utils.ModelTool.*;
import io.github.jiangood.openadmin.util.PageTool;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ModelService {

    private final RepositoryService repositoryService;

    public Page<ModelPageVO> page(String searchText, Pageable pageable) {
        ModelQuery query = repositoryService.createModelQuery();
        if (searchText != null) {
            query.modelNameLike(searchText);
        }
        Page<Model> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, ModelPageVO::from);
    }

    public Map<String, Object> detail(String id) {
        Model model = repositoryService.getModel(id);
        Assert.notNull(model, "流程未定义");
        byte[] source = repositoryService.getModelEditorSource(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", model.getName());
        data.put("key", model.getKey());
        data.put("content", new String(source, StandardCharsets.UTF_8));
        return data;
    }

    public void delete(String id) {
        repositoryService.deleteModel(id);
    }

    public void saveContent(String id, String content) {
        Assert.hasText(content, "内容不能为空");
        repositoryService.addModelEditorSource(id, content.getBytes(StandardCharsets.UTF_8));
    }

    public void deploy(String id, String xml) {
        Assert.hasText(xml, "内容不能为空");
        repositoryService.addModelEditorSource(id, xml.getBytes(StandardCharsets.UTF_8));

        Model m = repositoryService.getModel(id);
        BpmnModel bpmnModel = xmlToModel(xml);

        Process mainProcess = bpmnModel.getMainProcess();
        mainProcess.setExecutable(true);
        String key = m.getKey();
        mainProcess.setId(key);
        mainProcess.setName(m.getName());

        validateModel(bpmnModel);

        String resourceName = m.getName() + ".bpmn20.xml";

        repositoryService.createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .name(m.getName())
                .key(key)
                .deploy();
    }

    public Page<Dict> definitionPage(String key, Pageable pageable) {
        Assert.notNull(key, "编码不能为空");
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .orderByProcessDefinitionVersion().desc();

        Page<ProcessDefinition> page = FlowablePageTool.queryPage(query, pageable);
        return PageTool.convert(page, d -> Dict.create()
                .set("id", d.getId())
                .set("key", d.getKey())
                .set("name", d.getName())
                .set("version", d.getVersion())
        );
    }

    public String getDefinitionContent(String id) {
        Assert.notNull(id, "id不能为空");
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
        return modelToXml(bpmnModel);
    }
}
```

- [ ] **Step 2: Create ModelOptionsService.java**

```java
package io.github.jiangood.openadmin.modules.flowable.model;

import io.github.jiangood.openadmin.framework.data.specification.Spec;
import io.github.jiangood.openadmin.modules.flowable.domain.FormDefinition;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessVariable;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService;
import io.github.jiangood.openadmin.modules.system.entity.SysRole;
import io.github.jiangood.openadmin.modules.system.entity.SysUser;
import io.github.jiangood.openadmin.modules.system.service.SysRoleService;
import io.github.jiangood.openadmin.modules.system.service.SysUserService;
import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.util.annotation.RemarkTool;
import io.github.jiangood.openadmin.util.dto.Option;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class ModelOptionsService {

    private final SysUserService sysUserService;
    private final SysRoleService roleService;
    private final RepositoryService repositoryService;
    private final ProcessMetaService processMetaService;

    public List<Option> javaDelegateOptions() {
        Map<String, JavaDelegate> beans = SpringTool.getBeansOfType(JavaDelegate.class);
        List<Option> options = new ArrayList<>();
        for (Map.Entry<String, JavaDelegate> e : beans.entrySet()) {
            String beanName = e.getKey();
            JavaDelegate value = e.getValue();
            Class<? extends JavaDelegate> cls = value.getClass();
            String remark = RemarkTool.getRemark(cls);
            String label = remark == null ? beanName : remark;
            String key = "${" + beanName + "}";
            options.add(new Option(key, label));
        }
        return options;
    }

    public List<Option> formOptions(String code) {
        ProcessMeta meta = processMetaService.findOne(code);
        List<FormDefinition> formList = meta.getForms();
        if (formList == null) {
            formList = new ArrayList<>();
        }
        return Option.convertList(formList, FormDefinition::getKey, FormDefinition::getLabel);
    }

    public List<Option> assigneeOptions(String searchText) {
        List<Option> list = queryUserOptions(searchText);
        list.add(0, new Option("${INITIATOR}", "发起人"));
        list.add(1, new Option("${INITIATOR_DEPT_LEADER}", "部门负责人"));
        return list;
    }

    public List<Option> candidateGroupsOptions() {
        List<SysRole> roleList = roleService.findAll(Sort.by("seq", "name"));
        List<Option> list = new ArrayList<>();
        for (SysRole sysRole : roleList) {
            list.add(new Option(sysRole.getId(), sysRole.getName()));
        }
        return list;
    }

    public List<Option> candidateUsersOptions(String searchText) {
        return queryUserOptions(searchText);
    }

    public List<ProcessVariable> varOptions(String code) {
        ProcessMeta meta = processMetaService.findOne(code);
        return meta.getVariables();
    }

    private List<Option> queryUserOptions(String searchText) {
        Spec<SysUser> spec = Spec.of();
        List<SysUser> userList = sysUserService.findAll(
                spec.orLike(searchText, "name", "account", "phone"),
                Sort.by("name"));
        List<Option> list = new ArrayList<>();
        for (SysUser sysUser : userList) {
            list.add(new Option(sysUser.getId(), sysUser.getName()));
        }
        return list;
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/model/ModelService.java
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/model/ModelOptionsService.java
git commit -m "refactor: extract ModelService and ModelOptionsService from ModelController"
```

---

### Task 3: Thin ModelController + move to model/ package

**Files:**
- Modify: Redesign and move `controller/ModelController.java` → `model/ModelController.java`
- Create: `model/ModelRequest.java`

The thinned controller only handles request mapping and delegates to services. Both inner records (`ModelRequest`, `ModelPageVO` references) are handled — `ModelPageVO` moves with the model module, `ModelRequest` becomes a separate file.

- [ ] **Step 1: Create ModelRequest.java**

```java
package io.github.jiangood.openadmin.modules.flowable.model;

public record ModelRequest(String id, String content) {
}
```

- [ ] **Step 2: Rewrite ModelController.java in model/ package — thin version**

```java
package io.github.jiangood.openadmin.modules.flowable.model;

import cn.hutool.core.lang.Dict;
import io.github.jiangood.openadmin.framework.log.Log;
import io.github.jiangood.openadmin.modules.flowable.domain.ProcessVariable;
import io.github.jiangood.openadmin.util.dto.AjaxResult;
import io.github.jiangood.openadmin.util.dto.IdReq;
import io.github.jiangood.openadmin.util.dto.Option;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("admin/flowable/model")
@AllArgsConstructor
public class ModelController {

    private final ModelService modelService;
    private final ModelOptionsService modelOptionsService;

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @RequestMapping("page")
    public AjaxResult page(String searchText, Pageable pageable) {
        Page<ModelPageVO> page = modelService.page(searchText, pageable);
        return AjaxResult.ok().data(page);
    }

    @GetMapping("detail")
    public AjaxResult detail(String id) {
        return AjaxResult.ok().data(modelService.detail(id));
    }

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("delete")
    public AjaxResult delete(@Valid @RequestBody IdReq id) {
        modelService.delete(id.getId());
        return AjaxResult.ok().msg("删除模型成功");
    }

    @PreAuthorize("hasAuthority('flowableModel:design')")
    @PostMapping("saveContent")
    public AjaxResult saveContent(@RequestBody ModelRequest param) {
        modelService.saveContent(param.id(), param.content());
        return AjaxResult.ok().msg("保存成功");
    }

    @Log("部署流程模型")
    @PreAuthorize("hasAuthority('flowableModel:deploy')")
    @PostMapping("deploy")
    public AjaxResult deploy(@RequestBody ModelRequest param) {
        modelService.deploy(param.id(), param.content());
        return AjaxResult.ok().msg("部署成功");
    }

    @GetMapping("javaDelegateOptions")
    public AjaxResult javaDelegateOptions() {
        return AjaxResult.ok().data(modelOptionsService.javaDelegateOptions());
    }

    @GetMapping("formOptions")
    public AjaxResult formOptions(String code) {
        return AjaxResult.ok().data(modelOptionsService.formOptions(code));
    }

    @GetMapping("assigneeOptions")
    public AjaxResult assigneeOptions(String searchText) {
        return AjaxResult.ok().data(modelOptionsService.assigneeOptions(searchText));
    }

    @GetMapping("candidateGroupsOptions")
    public AjaxResult candidateGroupsOptions() {
        return AjaxResult.ok().data(modelOptionsService.candidateGroupsOptions());
    }

    @GetMapping("candidateUsersOptions")
    public AjaxResult candidateUsersOptions(String searchText) {
        return AjaxResult.ok().data(modelOptionsService.candidateUsersOptions(searchText));
    }

    @GetMapping("varList")
    public AjaxResult varOptions(String code) {
        return AjaxResult.ok().data(modelOptionsService.varOptions(code));
    }

    @GetMapping("definitionPage")
    public AjaxResult definitionPage(String key, Pageable pageable) {
        return AjaxResult.ok().data(modelService.definitionPage(key, pageable));
    }

    @GetMapping("getDefinitionContent")
    public AjaxResult getDefinitionContent(String id) {
        return AjaxResult.ok().data(modelService.getDefinitionContent(id));
    }
}
```

- [ ] **Step 3: Move ModelPageVO.java to model/ package**

In `model/ModelPageVO.java`:
```
package io.github.jiangood.openadmin.modules.flowable.model;
```
Update the import for `ModelPageVO` — it uses only Flowable `Model` class internally, no cross-module imports needed.

- [ ] **Step 4: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/model/
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/controller/ModelController.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/vo/ModelPageVO.java
git commit -m "refactor: thin ModelController and move to model/ package"
```

---

### Task 4: Move process/ module files

**Files:**
- Move: `FlowableTemplate.java` (root) → `process/FlowableTemplate.java`
- Move: `domain/ProcessMeta.java` → `process/ProcessMeta.java`
- Move: `domain/ProcessVariable.java` → `process/ProcessVariable.java`
- Move: `domain/FormDefinition.java` → `process/FormDefinition.java`
- Move: `service/ProcessMetaService.java` → `process/ProcessMetaService.java`
- Move: `service/ProcessService.java` → `process/ProcessService.java`

Package: `io.github.jiangood.openadmin.modules.flowable.process`

- [ ] **Step 1: Move FlowableTemplate.java — change package to `...flowable.process`**

Update import references in the file:
- `io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta` → `io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta`
- `io.github.jiangood.openadmin.modules.flowable.domain.ProcessVariable` → `io.github.jiangood.openadmin.modules.flowable.process.ProcessVariable`
- `io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService` → `io.github.jiangood.openadmin.modules.flowable.process.ProcessMetaService`
- `io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants` stays

- [ ] **Step 2: Move ProcessMeta.java, ProcessVariable.java, FormDefinition.java**

Package: `io.github.jiangood.openadmin.modules.flowable.process`

`ProcessMeta.java` import to update:
- `io.github.jiangood.openadmin.modules.flowable.listener.ProcessListener` stays (listener/ is unchanged)

`ProcessVariable.java` — no imports to update (uses `io.github.jiangood.openadmin.util.field.ValueType` from external lib)

`FormDefinition.java` — check contents:

```java
package io.github.jiangood.openadmin.modules.flowable.domain;

import lombok.Data;

@Data
public class FormDefinition {
    private String key;
    private String label;
}
```
→ change package to `io.github.jiangood.openadmin.modules.flowable.process`

- [ ] **Step 3: Move ProcessMetaService.java — change package to `...flowable.process`**

Update import: `io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta` → `io.github.jiangood.openadmin.modules.flowable.process.ProcessMeta`

- [ ] **Step 4: Move ProcessService.java — change package to `...flowable.process`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.config.FlowableProperties` stays (config/ is unchanged)
- `io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta` → `...flowable.process.ProcessMeta`
- `io.github.jiangood.openadmin.modules.flowable.dto.TaskHandleType` → `...flowable.common.dto.TaskHandleType`
- `io.github.jiangood.openadmin.modules.flowable.listener.ProcessListener` stays
- `io.github.jiangood.openadmin.modules.flowable.service.BpmnDiagramService` → `...flowable.diagram.BpmnDiagramService`
- `io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService` → `...flowable.process.ProcessMetaService`

- [ ] **Step 5: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/process/
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/FlowableTemplate.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/domain/ProcessMeta.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/domain/ProcessVariable.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/domain/FormDefinition.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/ProcessMetaService.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/ProcessService.java
git commit -m "refactor: move process module files"
```

---

### Task 5: Move task/ + monitor/ module files

**Files:**
- Move: `controller/UserTaskController.java` → `task/UserTaskController.java`
- Move: `service/UserTaskService.java` → `task/UserTaskService.java`
- Move: `dto/response/TaskResponse.java` → `task/TaskResponse.java`
- Move: `dto/response/CommentResponse.java` → `task/CommentResponse.java`
- Move: `controller/MonitorController.java` → `monitor/MonitorController.java`
- Move: `service/MonitorService.java` → `monitor/MonitorService.java`
- Move: `dto/response/MonitorTaskResponse.java` → `monitor/MonitorTaskResponse.java`
- Move: `dto/vo/ProcessDefinitionVO.java` → `monitor/ProcessDefinitionVO.java`
- Move: `dto/vo/ProcessInstanceVO.java` → `monitor/ProcessInstanceVO.java`
- Create: `monitor/SetAssigneeRequest.java`

- [ ] **Step 1: Move UserTaskController.java — change package to `...flowable.task`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.dto.request.HandleTaskRequest` → `...flowable.common.dto.HandleTaskRequest`
- `io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse` → `...flowable.task.TaskResponse`
- `io.github.jiangood.openadmin.modules.flowable.service.ProcessService` → `...flowable.process.ProcessService`
- `io.github.jiangood.openadmin.modules.flowable.service.UserTaskService` → `...flowable.task.UserTaskService`

- [ ] **Step 2: Move UserTaskService.java — change package to `...flowable.task`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.dto.response.CommentResponse` → `...flowable.task.CommentResponse`
- `io.github.jiangood.openadmin.modules.flowable.dto.response.TaskResponse` → `...flowable.task.TaskResponse`
- `io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool` → `...flowable.common.utils.FlowablePageTool`
- `io.github.jiangood.openadmin.modules.flowable.service.BpmnDiagramService` → `...flowable.diagram.BpmnDiagramService`

- [ ] **Step 3: Move TaskResponse.java — change package to `...flowable.task`**

No cross-module imports to update (uses only Lombok + standard types).

- [ ] **Step 4: Move CommentResponse.java — change package to `...flowable.task`**

No cross-module imports to update.

- [ ] **Step 5: Move MonitorController.java — change package to `...flowable.monitor`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.service.MonitorService` → `...flowable.monitor.MonitorService`

- [ ] **Step 6: Move MonitorService.java — change package to `...flowable.monitor`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.dto.vo.ProcessDefinitionVO` → `...flowable.monitor.ProcessDefinitionVO`
- `io.github.jiangood.openadmin.modules.flowable.dto.vo.ProcessInstanceVO` → `...flowable.monitor.ProcessInstanceVO`
- `io.github.jiangood.openadmin.modules.flowable.dto.response.MonitorTaskResponse` → `...flowable.monitor.MonitorTaskResponse`
- `io.github.jiangood.openadmin.modules.flowable.utils.FlowablePageTool` → `...flowable.common.utils.FlowablePageTool`
- `io.github.jiangood.openadmin.modules.flowable.service.UserTaskService` → `...flowable.task.UserTaskService`

- [ ] **Step 7: Move MonitorTaskResponse.java — change package to `...flowable.monitor`**

No cross-module imports to update.

- [ ] **Step 8: Move ProcessDefinitionVO.java — change package to `...flowable.monitor`**

No cross-module imports to update.

- [ ] **Step 9: Move ProcessInstanceVO.java — change package to `...flowable.monitor`**

No cross-module imports to update.

- [ ] **Step 10: Create SetAssigneeRequest.java**

```java
package io.github.jiangood.openadmin.modules.flowable.monitor;

import jakarta.validation.constraints.NotBlank;

public record SetAssigneeRequest(
        @NotBlank String taskId,
        @NotBlank String assignee) {
}
```

- [ ] **Step 11: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/task/
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/monitor/
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/controller/UserTaskController.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/controller/MonitorController.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/UserTaskService.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/MonitorService.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/response/TaskResponse.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/response/CommentResponse.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/response/MonitorTaskResponse.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/vo/ProcessDefinitionVO.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto/vo/ProcessInstanceVO.java
git commit -m "refactor: move task and monitor module files"
```

---

### Task 6: Move simulate/ + diagram/ module files

**Files:**
- Move: `controller/SimulateController.java` → `simulate/SimulateController.java`
- Move: `service/SimulateService.java` → `simulate/SimulateService.java`
- Move: `service/BpmnDiagramService.java` → `diagram/BpmnDiagramService.java`

- [ ] **Step 1: Move SimulateController.java — change package to `...flowable.simulate`**

Update import:
- `io.github.jiangood.openadmin.modules.flowable.service.SimulateService` → `...flowable.simulate.SimulateService`

- [ ] **Step 2: Move SimulateService.java — change package to `...flowable.simulate`**

Update imports:
- `io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants` stays
- `io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta` → `...flowable.process.ProcessMeta`
- `io.github.jiangood.openadmin.modules.flowable.service.BpmnDiagramService` → `...flowable.diagram.BpmnDiagramService`
- `io.github.jiangood.openadmin.modules.flowable.service.ProcessMetaService` → `...flowable.process.ProcessMetaService`

- [ ] **Step 3: Move BpmnDiagramService.java — change package to `...flowable.diagram`**

Update import:
- `io.github.jiangood.openadmin.modules.flowable.constant.FlowableConstants` stays

- [ ] **Step 4: Commit**

```bash
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/simulate/
git add open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/diagram/
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/controller/SimulateController.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/SimulateService.java
git rm open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service/BpmnDiagramService.java
git commit -m "refactor: move simulate and diagram module files"
```

---

### Task 7: Update external references + delete empty packages + verify build

**Files:**
- Modify: `open-admin-flowable-example/.../LeaveApplyController.java`
- Modify: `open-admin-flowable-starter/src/test/.../UserTaskServiceTest.java`
- Delete: empty `controller/`, `service/`, `domain/`, `dto/request/`, `dto/response/`, `dto/vo/`, `dto/` (if empty), `utils/` (if empty) directories

- [ ] **Step 1: Update LeaveApplyController.java**

Change import:
- `io.github.jiangood.openadmin.modules.flowable.FlowableTemplate` → `io.github.jiangood.openadmin.modules.flowable.process.FlowableTemplate`

- [ ] **Step 2: Update UserTaskServiceTest.java**

Change import:
- `io.github.jiangood.openadmin.modules.flowable.dto.response.CommentResponse` → `io.github.jiangood.openadmin.modules.flowable.task.CommentResponse`

- [ ] **Step 3: Delete empty directories**

```bash
# These should be empty after all moves:
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/controller
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/service
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/domain
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto\request
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto\response
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto\vo
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/dto
rmdir open-admin-flowable-starter/src/main/java/io/github/jiangood/openadmin/modules/flowable/utils
```

- [ ] **Step 4: Build and verify**

```bash
cd open-admin-flowable-starter
mvnw clean compile -q
```

Verify: BUILD SUCCESS. No compilation errors.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: update external references, clean up empty packages"
```
