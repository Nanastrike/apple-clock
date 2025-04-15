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

}
