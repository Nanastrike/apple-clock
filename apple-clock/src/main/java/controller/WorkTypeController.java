package controller;

import model.WorkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.WorkTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/work-types")
public class WorkTypeController {
    private final WorkTypeService workTypeService;

    @Autowired
    public WorkTypeController(WorkTypeService workTypeService) {
        this.workTypeService = workTypeService;
    }

    @GetMapping
    public List<WorkType> getAllTypes(){
        return workTypeService.getAllTypes();
    }

    //@RequestBody： 把 JSON 请求体转换成 Java 对象
    @PostMapping
    public WorkType addType(@RequestBody WorkType workType){
        return workTypeService.addType(workType);
    }

    //@PathVariable 把 URL 路径里的变量（比如 /3）取出来
    @DeleteMapping("/{id}") //对应 DELETE /api/work-types/3，要删除 id=3 的分类
    public void deleteType(@PathVariable Long id) {
        workTypeService.deleteType(id);
    }

    //@RequestBody 把 JSON 请求体转换成 Java 对象
    @PutMapping //对应 PUT /api/work-types，更新一个对象（通过 JSON）
    public WorkType update(@RequestBody WorkType workType){
        return  workTypeService.update(workType);
    }

}
