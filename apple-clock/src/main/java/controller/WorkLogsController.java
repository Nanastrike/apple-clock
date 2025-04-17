package controller;

import model.WorkLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.WorkLogsService;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class WorkLogsController {

    private final WorkLogsService workLogsService;

    @Autowired
    public WorkLogsController(WorkLogsService workLogsService) {
        this.workLogsService = workLogsService;
    }

    @PostMapping("/start")
    public WorkLogs startLog(@RequestParam Long userId,@RequestParam Long workTypeId){
        return workLogsService.startLog(userId,workTypeId);
    }

    @PostMapping("/stop/{logId}")
    public WorkLogs stopLog(@PathVariable Long logId){
        return workLogsService.stopLog(logId);
    }

    @GetMapping("/recent/{userId}")
    public List<WorkLogs> getRecentLogs(@PathVariable Long userId) {
        return workLogsService.getRecentLogs(userId);
    }


}
