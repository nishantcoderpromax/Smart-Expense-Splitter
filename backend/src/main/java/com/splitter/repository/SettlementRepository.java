package com.splitter.repository;



import com.splitter.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupIdOrderBySettledAtDesc(Long groupId);
}