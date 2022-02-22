package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.TaskInfoDTO;
import dataTransferObjects.UpdateListsDTO;
import dataTransferObjects.WorkerTaskInfoDto;

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

    public List<TaskTarget> getTargetToExecute() {
        return getTargetToExecute(1);
    /*    if (status == TaskStatus.ACTIVE)
            return ask.getWork();
        return null;*/
    }

    public List<TaskTarget> getTargetToExecute(int numberOfTaskToGet) {
        List<TaskTarget> l = new ArrayList<>();

        if (status == TaskStatus.ACTIVE) {
            for (int i = 0; i < numberOfTaskToGet; i++) {
                TaskTarget target = task.getWork();
                if (target == null) {
                    if (task.allGraphHasBeenProcessed)
                        setStatus(TaskStatus.FINISHED);
                    break;
                }
                target.targetPrice = task instanceof SimulationTask ? simulationPrice : compilationPrice;
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

    public TaskArgs getTaskArgsForNewTask(boolean isIncremental) {
        TaskArgs newTaskArgs = taskArgs.cloneArgs();
        if (!status.equals(TaskStatus.FINISHED))
            return null;
        if (isIncremental) {
            newTaskArgs.getTargetsSelectedForGraph().clear();
            newTaskArgs.getTargetsSelectedForGraph().addAll(task.getFailedAndSkippedList());
        }
        return newTaskArgs;
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

        return task.getUpdateListsDTO(registeredUsers.size());
    }

    public List<accumulatorForWritingToFile> getLogsDelta(int lastVisitedLog) {
        return task.fetchDeltaLogs(lastVisitedLog);
    }

    public List<String> getTargetClickedInfo(String targetName, TargetState targetState) {
        return task.getInfoAboutTargetInExecution(targetName, targetState);
    }

    public int finishWork(TaskTarget target, accumulatorForWritingToFile log) {
        task.finishWorkOnTarget(target, log);
        return simulationPrice == -1 ? compilationPrice : simulationPrice;
    }

    public WorkerTaskInfoDto getWorkerTaskinfo() {
        WorkerTaskInfoDto workerTaskInfoDto = new WorkerTaskInfoDto();
        workerTaskInfoDto.setTaskName(taskName);
        workerTaskInfoDto.setEnrolledUsers(registeredUsers.size());
        workerTaskInfoDto.setTaskProgress(task.getProgress());
        workerTaskInfoDto.setTargetPrice(simulationPrice == -1 ? compilationPrice : simulationPrice);
        return workerTaskInfoDto;
    }
}
