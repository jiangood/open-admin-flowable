package io.github.jiangood.openadmin.modules.flowable;

public class FlowableConstants {

    public static final String VAR_INITIATOR_DEPT_LEADER = "INITIATOR_DEPT_LEADER";

    /** Flowable internal delete reason prefix: activity move/reject operation */
    public static final String DELETE_REASON_CHANGE_ACTIVITY_PREFIX = "Change activity to ";

    // 变量KEY
    public static final String VAR_USER_ID = "userId";
    public static final String VAR_USER_NAME = "userName";
    public static final String VAR_UNIT_ID = "unitId";
    public static final String VAR_UNIT_NAME = "unitName";
    public static final String VAR_DEPT_ID = "deptId";
    public static final String VAR_DEPT_NAME = "deptName";

    /** 仿真标记变量 — 仿真实例注入此变量，查询时过滤 */
    public static final String VAR_SIMULATION = "_process_simulation";
}
