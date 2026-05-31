package io.github.jiangood.openadmin.modules.flowable.dao;

import io.github.jiangood.openadmin.modules.flowable.domain.ProcessMeta;

import java.util.List;

public interface IProcessMetaDao {

    List<ProcessMeta> findProcessMetaList();


}
