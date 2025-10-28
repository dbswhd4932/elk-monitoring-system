package com.example.monitoring.service;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DemoService {

    private final Counter greetingCounter;
    private final Counter dataProcessCounter;
    private final Random random = new Random();

    public DemoService(MeterRegistry meterRegistry) {
        this.greetingCounter = Counter.builder("greeting.invocations")
                .description("Number of times greeting method has been invoked")
                .tag("service", "demo")
                .register(meterRegistry);

        this.dataProcessCounter = Counter.builder("data.process.invocations")
                .description("Number of times data processing has been invoked")
                .tag("service", "demo")
                .register(meterRegistry);
    }

    @Timed(value = "greeting.time", description = "Time taken to return greeting")
    public String greet(String name) {
        greetingCounter.increment();
        return "Hello, " + name + "!";
    }

    @Timed(value = "slow.operation.time", description = "Time taken for slow operation")
    public String slowOperation() throws InterruptedException {
        int delay = random.nextInt(1000) + 500; // 500-1500ms
        Thread.sleep(delay);
        return "Slow operation completed in " + delay + "ms";
    }

    @Timed(value = "fast.operation.time", description = "Time taken for fast operation")
    public String fastOperation() {
        return "Fast operation completed!";
    }

    @Timed(value = "data.process.time", description = "Time taken to process data")
    public String processData(String data) {
        dataProcessCounter.increment();
        return "Processed data: " + data;
    }
}
