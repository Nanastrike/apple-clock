package service;

import model.Misc;
import repository.MiscRepository;

public class MiscService {

    private final MiscRepository miscRepository;

    public MiscService(MiscRepository miscRepository) {
        this.miscRepository = miscRepository;
    }

    /**
     * 读取当前设置（默认取ID=1的那一条）
     */
    public Misc getSettings() {
        Misc misc = miscRepository.findById(1L); // 直接根据 ID=1 查
        if (misc == null) { // 如果数据库里没有
            Misc defaultMisc = new Misc(); // 创建一个默认的 Misc
            defaultMisc.setId(1L); // 确保 ID 是 1
            miscRepository.save(defaultMisc);
            return defaultMisc;
        }
        return misc;
    }

    /**
     * 保存修改
     */
    public Misc updateSettings(Misc misc) {
        return miscRepository.save(misc);
    }
}
