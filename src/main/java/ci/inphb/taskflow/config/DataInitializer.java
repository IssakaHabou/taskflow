package ci.inphb.taskflow.config;

import ci.inphb.taskflow.model.Priority;
import ci.inphb.taskflow.model.Status;
import ci.inphb.taskflow.model.Task;
import ci.inphb.taskflow.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Insère quelques tâches de démonstration au premier démarrage (base vide).
 * Désactivable avec la propriété taskflow.demo-data=false.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final TaskRepository repository;
    private final boolean enabled;

    public DataInitializer(TaskRepository repository,
                           @Value("${taskflow.demo-data:true}") boolean enabled) {
        this.repository = repository;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || repository.count() > 0) {
            return;
        }
        repository.save(task("Créer le service JavaPaas sur Cloud Clusters",
                "Choisir le plan, la région et activer la base de données.", Status.TERMINEE, Priority.HAUTE, 0));
        repository.save(task("Déployer le JAR TaskFlow",
                "Construire avec mvn clean package puis téléverser target/taskflow.jar.", Status.EN_COURS, Priority.HAUTE, 1));
        repository.save(task("Connecter la base MySQL",
                "Renseigner DB_URL, DB_USERNAME et DB_PASSWORD puis activer le profil mysql.", Status.A_FAIRE, Priority.MOYENNE, 2));
        repository.save(task("Rédiger le rapport du TP",
                "Captures d'écran, architecture PaaS, comparaison IaaS / PaaS / SaaS.", Status.A_FAIRE, Priority.BASSE, 7));
        log.info("TaskFlow : {} tâches de démonstration insérées", repository.count());
    }

    private Task task(String title, String description, Status status, Priority priority, int dueInDays) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus(status);
        t.setPriority(priority);
        t.setDueDate(LocalDate.now().plusDays(dueInDays));
        return t;
    }
}
