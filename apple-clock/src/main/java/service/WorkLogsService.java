package service;

import model.WorkLogs;
import model.WorkType;
import repository.WorkLogsRepository;
import repository.WorkTypeRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class WorkLogsService {

    private final WorkLogsRepository workLogsRepository;
    private final WorkTypeRepository workTypeRepository;

    public WorkLogsService(WorkLogsRepository workLogsRepository, WorkTypeRepository workTypeRepository) {
        this.workLogsRepository = workLogsRepository;
        this.workTypeRepository = workTypeRepository;
    }

    // 开始新的计时记录
    public WorkLogs startLog(Long workTypeId) {
        WorkType workType = workTypeRepository.findById(workTypeId);
        if (workType == null) {
            System.out.println("该事件类型不存在！");
            return null;
        }
        WorkLogs log = new WorkLogs();
        log.setWorkType(workType);
        log.setBegin(LocalDateTime.now());

        return workLogsRepository.save(log);
    }

    // 结束正在进行的记录
    public WorkLogs stopLog(Long logId) {
        WorkLogs log = workLogsRepository.findById(logId);
        if (log == null) {
            System.out.println("未找到记录！");
            return null;
        }
        log.setEnd(LocalDateTime.now());
        Duration duration = Duration.between(log.getBegin(), log.getEnd());
        log.setDuration((int) duration.toMinutes());
        return workLogsRepository.save(log);
    }

    // 查询最近三条记录
    public List<WorkLogs> getRecentLogs() {
        return workLogsRepository.findTop3OrderByBeginDesc();
    }
}
