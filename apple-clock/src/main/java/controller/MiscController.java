package controller;

import model.Misc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.MiscService;

@RestController
@RequestMapping("/api/misc")
public class MiscController {

    private final MiscService miscService;

    @Autowired
    public MiscController(MiscService miscService) {
        this.miscService = miscService;
    }

    @GetMapping
    public Misc getSettings() {
        return miscService.getSettings();
    }

    @PostMapping
    public Misc updateSettings(@RequestBody Misc misc) {
        return miscService.updateSettings(misc);
    }
}
