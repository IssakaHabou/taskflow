package ci.inphb.taskflow.repository;

import ci.inphb.taskflow.model.Status;
import ci.inphb.taskflow.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusOrderByCreatedAtDesc(Status status);

    List<Task> findAllByOrderByCreatedAtDesc();

    long countByStatus(Status status);
}
