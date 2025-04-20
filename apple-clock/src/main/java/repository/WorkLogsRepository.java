package repository;

import model.WorkLogs;
import model.WorkType;

import java.time.LocalDate;
import java.util.List;

public interface WorkLogsRepository  {

    List<WorkLogs> findTop3OrderByBeginDesc();
    WorkLogs findById(Long id);
    WorkLogs save(WorkLogs workLogs);
    void delete(WorkLogs workLogs);
    List<WorkLogs> findAll();
    List<WorkLogs> findByDate(LocalDate date);
    List<WorkLogs> findByWorkType(WorkType workType);
    List<WorkLogs> findByDateRange(LocalDate begin, LocalDate end);
    List<WorkLogs> findByDateAndWorkType(LocalDate date, WorkType workType);
    List<WorkLogs> findByDurationGreaterThanEqual(int durationMinutes);
    List<WorkLogs> findByDateRangeAndWorkType(LocalDate begin, LocalDate end, WorkType workType);
}
