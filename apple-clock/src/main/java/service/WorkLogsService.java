package service;

import model.User;
import model.WorkLogs;
import model.WorkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.UserRepository;
import repository.WorkLogsRepository;
import repository.WorkTypeRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkLogsService {
    private final WorkLogsRepository workLogsRepository;
    private final UserRepository userRepository;
    private final WorkTypeRepository workTypeRepository;

    @Autowired
    public WorkLogsService(WorkLogsRepository workLogsRepository, UserRepository userRepository, WorkTypeRepository workTypeRepository) {
        this.workLogsRepository = workLogsRepository;
        this.userRepository = userRepository;
        this.workTypeRepository = workTypeRepository;
    }

    public WorkLogs startLog(Long userid, Long workTypeId){
        //这个方法的返回值是 Optional<T>，代表“可能有，也可能没有”。
        User user = userRepository.findById(userid).orElse(null);
        if(user == null){
            System.out.println("the user doesn't exist");
            return null;
        }
        WorkType workType = workTypeRepository.findById(workTypeId).orElse(null);
        if (workType == null) {
            System.out.println("this type doesn't exist");
            return null;
        }
        // 3. 创建 WorkLogs 实例
        WorkLogs log = new WorkLogs();
        log.setUser(user);
        log.setWorkType(workType);
        log.setBegin(LocalDateTime.now());

        return workLogsRepository.save(log);
    }


    public WorkLogs stopLog(Long logId){
        WorkLogs logs = workLogsRepository.findById(logId).orElse(null);
        if (logs == null) {
            System.out.println("no record found");
            return null;
        }
        logs.setEnd(LocalDateTime.now());
        //calculate the duration of the time
        Duration duration = Duration.between(logs.getBegin(), logs.getEnd());
        logs.setDuration((int) duration.toMinutes());
        return workLogsRepository.save(logs);
    }


    public List<WorkLogs> getRecentLogs(Long userId){
        return workLogsRepository.findTop3ByUserIdOrderByBeginDesc(userId);
    }

}
