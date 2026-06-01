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
