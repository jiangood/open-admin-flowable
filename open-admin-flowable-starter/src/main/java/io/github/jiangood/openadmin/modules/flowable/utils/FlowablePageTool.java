package io.github.jiangood.openadmin.modules.flowable.utils;

import org.flowable.common.engine.api.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FlowablePageTool {

    public static <T extends Query<?, ?>, U> Page<U> queryPage(Query<T, U> query, Pageable pageable) {
        long count = query.count();
        List<U> list = query.listPage((int) pageable.getOffset(), pageable.getPageSize());

        return new PageImpl<>(list, pageable, count);
    }
}
