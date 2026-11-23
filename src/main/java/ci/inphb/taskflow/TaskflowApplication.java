package ci.inphb.taskflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de TaskFlow.
 * L'application embarque son propre serveur (Tomcat) : elle se lance avec
 * "java -jar taskflow.jar", ce qui la rend directement déployable sur un PaaS.
 */
@SpringBootApplication
public class TaskflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskflowApplication.class, args);
    }
}
