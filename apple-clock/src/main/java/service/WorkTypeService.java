package service;

import model.WorkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.WorkTypeRepository;

import java.util.List;

@Service
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;

    @Autowired
    public WorkTypeService(WorkTypeRepository workTypeRepository) {
        this.workTypeRepository = workTypeRepository;
    }

    public List<WorkType> getAllTypes(){
        return workTypeRepository.findAll();
    }

    public WorkType addType(WorkType workType){
        List<WorkType> list = getAllTypes();
        boolean duplicate = false;
        for (WorkType i:list) {
                if (workType.getName().equals(i.getName())) {
                    duplicate = true;
                    break;
                }
            }
        if (duplicate){
            System.out.println("Duplicate type name, please enter a new name");
            return null;
        }
    return workTypeRepository.save(workType);
    }

public void deleteType(Long id){
        workTypeRepository.deleteById(id);
}

public WorkType update(WorkType workType){
    List<WorkType> list = getAllTypes();
    boolean duplicate = false;
    for (WorkType i:list) {
        if (workType.getName().equals(i.getName())&& !i.getId().equals(workType.getId())) {
            duplicate = true;
            break;
        }
    }
    if (duplicate){
        System.out.println("Duplicate type name, please enter a new name");
        return null;
    }
    return workTypeRepository.save(workType);
}

    /**
     * 检查数据库，如果没有事件类型，初始化默认的三条数据
     */
    public void initializeDefaultWorkTypes() {
        if (workTypeRepository.findAll().isEmpty()){
            WorkType learn = new WorkType("学习", "#A3BFD9");   // 莫兰迪蓝
            WorkType draw = new WorkType("运动", "#C1A3BF");    // 莫兰迪紫
            WorkType read = new WorkType("看书", "#A3C1A3");    // 莫兰迪绿

            workTypeRepository.save(learn);
            workTypeRepository.save(draw);
            workTypeRepository.save(read);

            System.out.println("默认事件类型初始化成功！");
        } else {
            System.out.println("已存在事件类型，无需初始化。");
        }
    }
}
