package ci.inphb.taskflow.web;

import ci.inphb.taskflow.model.Priority;
import ci.inphb.taskflow.model.Status;
import ci.inphb.taskflow.model.Task;
import ci.inphb.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.sql.Connection;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pages web (Thymeleaf) : liste des tâches, formulaire, page « Plateforme ».
 */
@Controller
public class PageController {

    private final TaskService service;
    private final Environment env;
    private final DataSource dataSource;

    @Value("${spring.application.name:TaskFlow}")
    private String appName;

    public PageController(TaskService service, Environment env, DataSource dataSource) {
        this.service = service;
        this.env = env;
        this.dataSource = dataSource;
    }

    @ModelAttribute
    void commons(Model model) {
        model.addAttribute("statuses", Status.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("appName", appName);
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) Status status, Model model) {
        model.addAttribute("tasks", service.findAll(status));
        model.addAttribute("stats", service.stats());
        model.addAttribute("filter", status);
        return "index";
    }

    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        model.addAttribute("task", new Task());
        return "form";
    }

    @GetMapping("/tasks/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("task", service.findById(id));
        return "form";
    }

    @PostMapping("/tasks")
    public String save(@Valid @ModelAttribute("task") Task task, BindingResult result,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "form";
        }
        if (task.getId() == null) {
            service.create(task);
            redirect.addFlashAttribute("message", "Tâche créée.");
        } else {
            service.update(task.getId(), task);
            redirect.addFlashAttribute("message", "Tâche mise à jour.");
        }
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam Status value) {
        service.changeStatus(id, value);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("message", "Tâche supprimée.");
        return "redirect:/";
    }

    /** Page de démonstration PaaS : montre l'environnement d'exécution fourni par la plateforme. */
    @GetMapping("/plateforme")
    public String platform(Model model) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("Version Java", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        info.put("Version Spring Boot", SpringBootVersion.getVersion());
        info.put("Système d'exploitation", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " / " + System.getProperty("os.arch"));
        info.put("Nom d'hôte (conteneur)", hostname());
        info.put("Processeurs disponibles", String.valueOf(Runtime.getRuntime().availableProcessors()));
        info.put("Mémoire JVM max", (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " Mo");
        info.put("Mémoire JVM utilisée", ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                / (1024 * 1024)) + " Mo");
        info.put("Port d'écoute", env.getProperty("local.server.port", env.getProperty("server.port", "8080")));
        String[] profiles = env.getActiveProfiles();
        info.put("Profil actif", profiles.length == 0 ? "default (H2)" : String.join(", ", Arrays.asList(profiles)));
        info.put("Base de données", databaseInfo());
        info.put("Temps de fonctionnement", formatUptime(ManagementFactory.getRuntimeMXBean().getUptime()));
        model.addAttribute("info", info);
        return "plateforme";
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "inconnu";
        }
    }

    private String databaseInfo() {
        try (Connection c = dataSource.getConnection()) {
            var meta = c.getMetaData();
            return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
        } catch (Exception e) {
            return "indisponible (" + e.getMessage() + ")";
        }
    }

    private String formatUptime(long millis) {
        Duration d = Duration.ofMillis(millis);
        return String.format("%d h %02d min %02d s", d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }
}
