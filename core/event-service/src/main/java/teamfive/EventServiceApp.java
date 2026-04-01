package teamfive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "teamfive",
        "teamfive.client",
        "dto"
})
@EnableFeignClients(basePackages = "teamfive.feignclient")
public class EventServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApp.class, args);
    }
}
