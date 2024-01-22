package service;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static service.TaskConstants.*;

@RestController
class TaskController {
    private List<Task> tasks = new ArrayList<>();

    @PostMapping(value =  "/processTasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processTasks(@RequestBody Map<String, List<Task>> receivedTasks) {
        tasks = receivedTasks.get("tasks");
        try {
            List<Task> sortedTasks = sortTasks();
            return ResponseEntity.ok(sortedTasks);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/generateBashScript")
    public String generateBashScript() {
        if(!tasks.isEmpty()) {
            List<Task> sortedTasks = sortTasks();
            return generateBashScript(sortedTasks);
        } else {
            return BASH_SCRIPT_NO_TASKS_LIST_ERROR;
        }
    }

    private List<Task> sortTasks() {
        List<Task> sortedTasks = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> currentlyVisiting = new HashSet<>();
        Set<String> taskNames = new HashSet<>();

        if(tasks == null || tasks.isEmpty()){
            throw new IllegalArgumentException(EMPTY_TASK_LIST_ERROR);
        }

        for (Task task : tasks) {
            String taskName = task.getName();
            if (taskNames.contains(taskName)) {
                throw new IllegalArgumentException(String.format(DUPLICATE_TASK_NAME_ERROR, taskName));
            }
            taskNames.add(taskName);

            if (!visited.contains(task.getName())) {
                resolveDependencies(task, sortedTasks, visited, currentlyVisiting);
            }
        }

        return sortedTasks;
    }

    private void resolveDependencies(Task task, List<Task> sortedTasks, Set<String> visited, Set<String> currentlyVisiting) {
        if(task.getName() == null || task.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_TASK_NAME_ERROR);
        }

        if(task.getCommand() == null || task.getCommand().trim().isEmpty()) {
            throw new IllegalArgumentException(INVALID_TASK_COMMAND_ERROR);
        }

        String taskName = task.getName();

        if (currentlyVisiting.contains(taskName)) {
            throw new RuntimeException(String.format(CYCLIC_DEPENDENCIES_IN_TASK_ERROR, taskName));
        }

        if (!visited.contains(taskName)) {
            currentlyVisiting.add(taskName);

            if (task.requires != null) {
                for (String dependencyName : task.requires) {
                    if (dependencyName == null) {
                        throw new IllegalArgumentException(String.format(MISSING_DEPENDENCY_NAME_IN_TASK_ERROR, task.getName()));
                    }
                    if (dependencyName.equals(task.getName())) {
                        throw new IllegalArgumentException(String.format(TASK_CANNOT_DEPEND_ON_ITSELF_ERROR, task.getName()));
                    }

                    Task dependency = tasks.stream()
                            .filter(t -> t.getName().equals(dependencyName))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(String.format(TASK_DEPENDENCY_NOT_FOUND_ERROR, taskName, dependencyName)));

                    resolveDependencies(dependency, sortedTasks, visited, currentlyVisiting);
                }
            }

            currentlyVisiting.remove(taskName);
            visited.add(taskName);
            sortedTasks.add(task);
        }
    }

    private String generateBashScript(List<Task> sortedTasks) {
        StringBuilder script = new StringBuilder(BASH_SCRIPT_SHEBANG + "\n");
        for (Task task : sortedTasks) {
            script.append(task.command).append("\n");
        }
        return script.toString();
    }
}