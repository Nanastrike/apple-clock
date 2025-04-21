package service;

import model.WorkType;
import repository.WorkTypeRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;

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

public void deleteType(List<Long> ids){
    ids.forEach(workTypeRepository::deleteById);
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
            WorkType learn = new WorkType("学习");   // 莫兰迪蓝
            WorkType draw = new WorkType("运动");    // 莫兰迪紫
            WorkType read = new WorkType("看书");    // 莫兰迪绿

            workTypeRepository.save(learn);
            workTypeRepository.save(draw);
            workTypeRepository.save(read);

            System.out.println("默认事件类型初始化成功！");
        } else {
            System.out.println("已存在事件类型，无需初始化。");
        }
    }

    public WorkType findByName(String name) {
        List<WorkType> allTypes = workTypeRepository.findAll();
        for (WorkType type : allTypes) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null; // 找不到就返回 null
    }

    public List<WorkType> findByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();          // 空集合 → 返回空
        }
        return workTypeRepository.findAll().stream()
                .filter(wt -> names.contains(wt.getName()))
                .collect(Collectors.toList());
    }


    public List<String> getAllWorkTypeNames() {
// 从仓库取出全部 WorkType，然后映射成名字列表
        List<WorkType> allTypes = workTypeRepository.findAll();
        if (allTypes == null || allTypes.isEmpty()) {
            return Collections.emptyList();
        }
        return allTypes.stream()
                .map(WorkType::getName)
                .collect(Collectors.toList());
    }
}
