package cz.cuni.matfyz.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jachym.bartik
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "Server is running.";
    }
}
