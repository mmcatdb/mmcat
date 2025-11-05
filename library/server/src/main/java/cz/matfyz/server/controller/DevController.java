package cz.matfyz.server.controller;

import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    @GetMapping("/ping")
    public String ping() {
        return new Date().toString();
    }

}
