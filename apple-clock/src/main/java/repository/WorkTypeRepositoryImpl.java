package repository;

import model.WorkType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkTypeRepositoryImpl implements WorkTypeRepository {

    private final List<WorkType> workTypes = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1); // 模拟ID自增

    @Override
    public WorkType save(WorkType workType) {
        if (workType.getId() == null) {
            workType.setId((long) idGenerator.getAndIncrement()); // 赋ID
        }
        workTypes.add(workType);
        return workType;
    }

    @Override
    public List<WorkType> findAll() {
        return new ArrayList<>(workTypes);
    }

    @Override
    public void deleteById(Long id) {
        workTypes.removeIf(workType -> workType.getId().equals(id));
    }

    @Override
    public WorkType findById(Long id) {
        return workTypes.stream()
                .filter(workType -> workType.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
