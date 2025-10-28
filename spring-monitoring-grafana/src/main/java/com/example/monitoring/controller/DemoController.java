package com.example.monitoring.controller;

import com.example.monitoring.service.DemoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final DemoService demoService;

    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "World") String name) {
        return demoService.greet(name);
    }

    @GetMapping("/slow")
    public String slowEndpoint() throws InterruptedException {
        return demoService.slowOperation();
    }

    @GetMapping("/fast")
    public String fastEndpoint() {
        return demoService.fastOperation();
    }

    @PostMapping("/data")
    public String createData(@RequestBody String data) {
        return demoService.processData(data);
    }

    @GetMapping("/error")
    public String errorEndpoint() {
        throw new RuntimeException("Intentional error for monitoring test");
    }
}
