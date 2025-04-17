package service;

import model.Misc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.MiscRepository;

import java.util.List;

@Service
public class MiscService {

    private final MiscRepository miscRepository;

    @Autowired
    public MiscService(MiscRepository miscRepository) {
        this.miscRepository = miscRepository;
    }

    // 读取当前设置（默认取ID=1的那一条）
    public Misc getSettings() {
        List<Misc> all = miscRepository.findAll();
        if (all.isEmpty()) {
            Misc defaultMisc = new Misc();
            miscRepository.save(defaultMisc);
            return defaultMisc;
        } else {
            return all.get(0); //本地项目默认只会有一个用户
        }
    }

    // 保存修改
    public Misc updateSettings(Misc misc) {
        return miscRepository.save(misc);
    }
}
