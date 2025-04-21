package service;

import jakarta.persistence.TypedQuery;
import model.WorkLogs;
import model.WorkType;
import repository.WorkLogsRepository;
import repository.WorkTypeRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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

        long seconds = Duration.between(log.getBegin(), log.getEnd()).getSeconds();
        int minutes  = Math.max(1, (int) Math.ceil(seconds / 60.0)); // ★ 向上取整，至少 1
        log.setDuration(minutes);

        return workLogsRepository.save(log);
    }

    // 查询最近三条记录
    public List<WorkLogs> getRecentLogs() {
        return workLogsRepository.findTop3OrderByBeginDesc();
    }

    public List<WorkLogs> findByDateRangeAndWorkNames(LocalDate s, LocalDate e, List<String> workNames) {
        /*  空集合 → 返回空，跟其他 DAO 方法保持一致 */
        if (workNames == null || workNames.isEmpty()) {
            return Collections.emptyList();
        }

        /*  把名称列表映射成 WorkType 实体列表 */
        List<WorkType> typeEntities = workTypeRepository.findAll().stream()
                .filter(wt -> workNames.contains(wt.getName()))
                .toList();

        if (typeEntities.isEmpty()) {  // 数据库里压根没有这些名字
            return Collections.emptyList();
        }

        /* 复用已有 DAO 方法 */
        return workLogsRepository.findByDateRangeAndWorkType(
                s, e, typeEntities);
    }
    public List<WorkLogs> findByDateRange(LocalDate begin, LocalDate end){
        return workLogsRepository.findByDateRange(begin, end);
    }
}
