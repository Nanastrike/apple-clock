package repository;

import model.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkTypeRepository extends JpaRepository<WorkType, Long> {
//inherit the method from JpaRepository
    //no need to add new method for now


}
