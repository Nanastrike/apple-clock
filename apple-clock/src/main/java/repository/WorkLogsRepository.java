package repository;

import model.WorkLogs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkLogsRepository extends JpaRepository<WorkLogs, Long> {
    List<WorkLogs> findByUserId(Long userId);

}
