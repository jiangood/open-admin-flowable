package io.github.jiangood.openadmin.modules.flowable.example.repository;

import io.github.jiangood.openadmin.modules.flowable.example.entity.LeaveApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveApplyRepository extends JpaRepository<LeaveApply, Long> {
    Optional<LeaveApply> findByBusinessKey(String businessKey);
}
