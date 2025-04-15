package service;

import model.WorkLogs;
import org.springframework.beans.factory.annotation.Autowired;
import repository.WorkLogsRepository;

public class WorkLogsService {
    private final WorkLogsRepository workLogsRepository;

    @Autowired
    public WorkLogsService(WorkLogsRepository workLogsRepository) {
        this.workLogsRepository = workLogsRepository;
    }

    public WorkLogs startLog(Long userid, Long workTypeId){

    }

}
