package service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Objects;

@JsonSerialize(using = TaskSerializer.class)
class Task {
    String name;
    String command;
    List<String> requires;

    public Task() {
    }

    public Task(String name, String command, List<String> requires) {
        this.name = name;
        this.command = command;
        this.requires = requires;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", command='" + command + '\'' +
                ", requires=" + requires +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return name.equals(task.name) &&
                command.equals(task.command) &&
                requires.equals(task.requires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, command, requires);
    }
}