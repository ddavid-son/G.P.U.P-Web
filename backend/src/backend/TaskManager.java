package backend;

import argumentsDTO.CommonEnums.*;
import backend.xmlhandler.GPUPTarget;
import com.sun.corba.se.impl.orbutil.graph.Graph;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.TaskInfoDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TaskManager {

    private Task task;
    private String taskName;
    private String ownerName;
    private String originalGraphName;
    private int simulationPrice = -1;
    private int compilationPrice = -1;
    private Set<String> registeredUsers = new HashSet<>();
    private TaskStatus status = TaskStatus.NEW;
    private final GraphInfoDTO targetDistrbution;
    private TaskInfoDTO myInfo;

    public String getTaskName() {
        return taskName;
    }

    public TaskInfoDTO getTaskDto() {
        if (myInfo == null) {
            myInfo = new TaskInfoDTO.Builder()
                    .setTaskName(taskName)
                    .setTaskOwner(ownerName)
                    .setTaskType(task instanceof SimulationTask ? TaskType.SIMULATION : TaskType.COMPILATION)
                    .setIsEnrolled(registeredUsers.contains(ownerName))
                    .setTaskStatus(status)
                    .setEnrolledWorker(new ArrayList<>(registeredUsers))
                    .setTargetDistribution(targetDistrbution)
                    .setPricePerTarget(task instanceof SimulationTask ? simulationPrice : compilationPrice)
                    .build();
        } else
            updateTaskInfo();

        return myInfo;
    }

    private void updateTaskInfo() {
        myInfo.setTaskStatus(status);
        myInfo.getEnrolledWorker().addAll(registeredUsers);
        myInfo.setEnrolled(registeredUsers.contains(ownerName));
    }

    public void getTargetsToExecute(String userName) {

    }

    public void activateTask() {
        task.run(System.out::println);
    }

    public void addUser(String userName) {
        registeredUsers.add(userName);
    }

    public void removeUser(String userName) {
        registeredUsers.remove(userName);
    }

    public TaskManager(Task task) {
        this.task = task;
        targetDistrbution = new GraphInfoDTO();
        task.graph.values().forEach(target -> targetDistrbution.countTargetsByType(target.getType()));
    }

    public void setManagerData(String originalGraph, String taskName, String taskOwner) {
        this.taskName = taskName;
        this.ownerName = taskOwner;
        this.originalGraphName = originalGraph;
    }

    public void setPrices(int simulationPrice, int compilationPrice) {
        if (task instanceof SimulationTask)
            this.simulationPrice = simulationPrice;
        else
            this.compilationPrice = compilationPrice;
    }

    public void pauseTask() {
        if (task != null)
            task.pauseTask();
    }

    public void resumeTask() {
        if (task != null)
            task.resumeTask();
    }

    public void setNumberOfThreads(Integer value) {
        if (task != null)
            task.changeNumberOfThreads(value);
    }
}
