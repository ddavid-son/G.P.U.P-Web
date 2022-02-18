package backend;

import argumentsDTO.CommonEnums.*;
import argumentsDTO.TaskArgs;
import argumentsDTO.TaskTarget;
import argumentsDTO.accumulatorForWritingToFile;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.TaskInfoDTO;
import dataTransferObjects.UpdateListsDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskManager {

    private final Task task;
    private String taskName;
    private String ownerName;
    private TaskArgs taskArgs;
    private TaskInfoDTO myInfo;
    private String originalGraphName;
    private int simulationPrice = -1;
    private int compilationPrice = -1;
    private TaskStatus status = TaskStatus.NEW;
    private final GraphInfoDTO targetDistrbution;
    private final Set<String> registeredUsers = new HashSet<>();


    public String getTaskName() {
        return taskName;
    }

    public TaskInfoDTO getTaskDto(String userName) {
        if (myInfo == null) {
            myInfo = new TaskInfoDTO.Builder()
                    .setTaskName(taskName)
                    .setTaskOwner(ownerName)
                    .setTaskType(task instanceof SimulationTask ? TaskType.SIMULATION : TaskType.COMPILATION)
                    .setIsEnrolled(registeredUsers.contains(userName))
                    .setTaskStatus(status)
                    .setEnrolledWorker(new ArrayList<>(registeredUsers))
                    .setTargetDistribution(targetDistrbution)
                    .setPricePerTarget(task instanceof SimulationTask ? simulationPrice : compilationPrice)
                    .build();
        } else
            updateTaskInfo(userName);

        return myInfo;
    }

    public String getStatus() {
        return status.toString();
    }

    private void updateTaskInfo(String userName) {
        myInfo.setTaskStatus(status);
        myInfo.getEnrolledWorker().clear();
        myInfo.getEnrolledWorker().addAll(registeredUsers);
        myInfo.setEnrolled(registeredUsers.contains(userName));
    }

    public void activateTask() {
        setStatus(TaskStatus.ACTIVE);
        if (status == TaskStatus.ACTIVE) {
            task.activateRun();
        }
    }

    public TaskTarget getTargetToExecute() {
        if (status == TaskStatus.ACTIVE)
            return task.getWork();
        return null;
    }

    public List<TaskTarget> getTargetToExecute(int numberOfTaskToGet) {
        List<TaskTarget> l = new ArrayList<>();

        if (status == TaskStatus.ACTIVE) {
            for (int i = 0; i < numberOfTaskToGet; i++) {
                TaskTarget target = task.getWork();
                if (target == null) {
                    if (task.allGraphHasBeenProcessed)
                        setStatus(TaskStatus.CANCELED);
                    break;
                }
                l.add(target);
            }
        }
        return l;
    }

    public TaskManager(Task task) {
        this.task = task;
        targetDistrbution = new GraphInfoDTO();
        task.graph.values().forEach(target -> targetDistrbution.countTargetsByType(target.getType()));
    }

    public void setTaskArgs(TaskArgs taskArgs) {

        this.taskArgs = taskArgs;
    }

    public TaskArgs getTaskArgs() {

        return taskArgs;
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

    public void setStatus(TaskStatus taskStatus) {
        if (status != TaskStatus.FINISHED && status != TaskStatus.CANCELED) {
            status = taskStatus;
        }
    }

    public void pauseTask() {
        setStatus(TaskStatus.PAUSED);
    }

    public void resumeTask() {
        setStatus(TaskStatus.ACTIVE);
    }

    public void cancelledTask() {
        setStatus(TaskStatus.CANCELED);
    }

    public void addWorker(String workerName) {

        registeredUsers.add(workerName);
    }

    public void removeWorker(String workerName) {

        registeredUsers.remove(workerName);
    }

    public UpdateListsDTO getUpdateListsDTO() {

        return task.getUpdateListsDTO();
    }

    public int finishWork(TaskTarget target, accumulatorForWritingToFile log) {
        task.finishWorkOnTarget(target, log);
        return simulationPrice == -1 ? compilationPrice : simulationPrice;
    }
}
