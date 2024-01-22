package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static service.TaskConstants.*;

class TaskControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
    }

    @Test
    void processTasks_ValidInput_ReturnsSortedTasks() throws Exception {
        //Given
        String validJsonString = "{\"tasks\": [{\"name\":\"task-1\",\"command\":\"command-1\",\"requires\":[\"task-2\"]}," +
                                              "{\"name\":\"task-2\",\"command\":\"command-2\",\"requires\":null}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJsonString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("task-2"))
                .andExpect(jsonPath("$[0].command").value("command-2"))
                .andExpect(jsonPath("$[0].requires").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("task-1"))
                .andExpect(jsonPath("$[1].command").value("command-1"))
                .andExpect(jsonPath("$[1].requires").doesNotExist());
    }

    @Test
    void processTasks_EmptyTaskList_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJsonString = "{\"tasks\": []}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(EMPTY_TASK_LIST_ERROR)))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void processTasks_DuplicateTaskName_ReturnsBadRequest() throws Exception {
        //Given
        String invalidJsonString = "{\"tasks\":[{\"name\":\"task-1\",\"command\":\"command-1\",\"requires\":null}," +
                                               "{\"name\":\"task-1\",\"command\":\"command-2\",\"requires\":null}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(DUPLICATE_TASK_NAME_ERROR, "task-1"))))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void processTasks_MissingTaskName_ReturnsBadRequest() throws Exception {
        //Given
        String invalidJsonString = "{\"tasks\":[{\"command\":\"command-1\",\"requires\":null}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(INVALID_TASK_NAME_ERROR)))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void processTasks_MissingCommand_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJsonString = "{\"tasks\":[{\"name\":\"task-1\",\"requires\":null}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(INVALID_TASK_COMMAND_ERROR)))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void processTasks_TaskNameSameAsDependency_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJsonString = "{\"tasks\":[{\"name\":\"task-1\",\"command\":\"command-1\",\"requires\":[\"task-1\"]}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(TASK_CANNOT_DEPEND_ON_ITSELF_ERROR, "task-1"))))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void processTasks_DependentTaskMissing_ReturnsBadRequest() throws Exception {
        // Given
        String invalidJsonString = "{\"tasks\":[{\"name\":\"task-1\",\"command\":\"command-1\",\"requires\":[\"task-2\"]}]}";

        // When & Then
        mockMvc.perform(post("/processTasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonString))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format(TASK_DEPENDENCY_NOT_FOUND_ERROR, "task-1", "task-2"))))
                .andExpect(jsonPath("$[0].requires").doesNotExist());
    }

    @Test
    void generateBashScript_ValidInput_ReturnsExpectedScript() {
        //Given
        List<Task> inputTasks = Arrays.asList(
                new Task("task-1", "echo 'Hello World!' > /tmp/file1", Arrays.asList("task-2")),
                new Task("task-2", "touch /tmp/file1", null)
        );
        Map<String, List<Task>> receivedTasks = new HashMap<>();
        receivedTasks.put("tasks", inputTasks);
        taskController.processTasks(receivedTasks);

        String expectedScript = "#!/usr/bin/env bash\ntouch /tmp/file1\necho 'Hello World!' > /tmp/file1\n";

        // When & Then
        String bash = taskController.generateBashScript();

        assertEquals(expectedScript,bash);
    }

    @Test
    void generateBashScript_NoTasksToRun_ReturnsErrorMessage() {
        // When & Then
        String bash = taskController.generateBashScript();

        assertEquals(BASH_SCRIPT_NO_TASKS_LIST_ERROR,bash);
    }
}