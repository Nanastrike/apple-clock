package repository;

import model.WorkType;
import java.util.List;


public interface WorkTypeRepository  {
//inherit the method from JpaRepository
    //no need to add new method for now

    WorkType save(WorkType workType);
    List<WorkType> findAll();
    void deleteById(Long id);
    WorkType findById(Long id);
}
