package ci.inphb.taskflow.service;

import ci.inphb.taskflow.model.Status;
import ci.inphb.taskflow.model.Task;
import ci.inphb.taskflow.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll(Status status) {
        return status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tâche introuvable : " + id));
    }

    public Task create(Task task) {
        task.setId(null);
        return repository.save(task);
    }

    public Task update(Long id, Task data) {
        Task task = findById(id);
        task.setTitle(data.getTitle());
        task.setDescription(data.getDescription());
        task.setStatus(data.getStatus());
        task.setPriority(data.getPriority());
        task.setDueDate(data.getDueDate());
        return repository.save(task);
    }

    public Task changeStatus(Long id, Status status) {
        Task task = findById(id);
        task.setStatus(status);
        return repository.save(task);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", repository.count());
        for (Status s : Status.values()) {
            stats.put(s.name(), repository.countByStatus(s));
        }
        return stats;
    }
}
