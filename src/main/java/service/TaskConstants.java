package service;

public class TaskConstants {
    // TaskController error messages
    public static final String EMPTY_TASK_LIST_ERROR = "Task list cannot be null or empty.";
    public static final String DUPLICATE_TASK_NAME_ERROR = "Duplicate task name found: %s";
    public static final String INVALID_TASK_NAME_ERROR = "Task name cannot be null or empty.";
    public static final String INVALID_TASK_COMMAND_ERROR = "Task command cannot be null or empty.";
    public static final String CYCLIC_DEPENDENCIES_IN_TASK_ERROR = "Cyclic requirements in tasks detected: %s";
    public static final String MISSING_DEPENDENCY_NAME_IN_TASK_ERROR = "Task %s has a dependency with null name";
    public static final String TASK_CANNOT_DEPEND_ON_ITSELF_ERROR = "Task %s cannot depend on itself";
    public static final String TASK_DEPENDENCY_NOT_FOUND_ERROR = "Dependency for %s not found %s";
    public static final String BASH_SCRIPT_NO_TASKS_LIST_ERROR = "Please first call /processTasks with a valid list of tasks.";

    // TaskController Bash script constants
    public static final String BASH_SCRIPT_SHEBANG = "#!/usr/bin/env bash";
}
