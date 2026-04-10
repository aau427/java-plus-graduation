package teamfive.starter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {
    private final AggregatorStarter appStarter;

    @Override
    public void run(String... args) {
        Thread aggregatorThread = new Thread(appStarter::start);
        aggregatorThread.setName("Aggregator-Executor");
        aggregatorThread.start();
    }
}