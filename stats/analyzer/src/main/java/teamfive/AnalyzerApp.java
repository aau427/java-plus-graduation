package teamfive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import teamfive.controller.AggregatorProcessor;
import teamfive.controller.CollectorProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(AnalyzerApp.class, args);

        final CollectorProcessor collectorProcessor =
                context.getBean(CollectorProcessor.class);
        final AggregatorProcessor aggregatorProcessor =
                context.getBean(AggregatorProcessor.class);

        Thread collectorThread = new Thread(collectorProcessor); // используем метод start
        collectorThread.setName("CollectorThread");
        collectorThread.start();

        Thread aggregatorEventsThread = new Thread(aggregatorProcessor);
        aggregatorEventsThread.setName("AggregatorEventHandlerThread");
        aggregatorEventsThread.start();

        log.info("Все процессоры запущены в фоновых потоках.");
    }
}
