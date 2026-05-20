package cz.matfyz.server;

import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    @GetMapping("/")
    public String index() {
        return "Server is running.";
    }

    @GetMapping("/ping")
    public String ping() {
        return new Date().toString();
    }

}
