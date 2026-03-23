package teamfive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "teamfive",
        "teamfive.client",
        "dto"
})
public class MainServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(MainServiceApp.class, args);
    }
}
