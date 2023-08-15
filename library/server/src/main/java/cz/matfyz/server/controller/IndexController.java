package cz.matfyz.server.controller;

import cz.matfyz.server.setup.ExampleSetup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    ExampleSetup exampleSetup;

    @PostMapping("/reset-database")
    public void updateCategoryWrapper() {
        exampleSetup.setup();
    }

}
