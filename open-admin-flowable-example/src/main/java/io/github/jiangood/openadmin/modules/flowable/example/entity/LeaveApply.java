package io.github.jiangood.openadmin.modules.flowable.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leave_apply")
public class LeaveApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String businessKey;

    private String reason;

    private Integer days;

    private Integer actualDays;

    private String leaveType;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
