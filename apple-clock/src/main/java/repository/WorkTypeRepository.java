package repository;

import model.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkTypeRepository  {
//inherit the method from JpaRepository
    //no need to add new method for now

    WorkType save(WorkType workType);
    List<WorkType> findAll();
    void deleteById(Long id); // ← 新加的
    WorkType findById(Long id); // ← 也可以顺便加
}
