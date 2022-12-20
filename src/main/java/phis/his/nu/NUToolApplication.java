package phis.his.nu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"phis.his.nu"})
public class NUToolApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(NUToolApplication.class);

        application.run(args);
    }

}
