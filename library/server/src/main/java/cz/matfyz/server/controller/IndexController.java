package cz.matfyz.server.controller;

import cz.matfyz.server.example.basic.ExampleSetup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/example-schema/{name}")
    public void addExampleSchema(@PathVariable String name) {
        exampleSetup.setup();
    }

}
