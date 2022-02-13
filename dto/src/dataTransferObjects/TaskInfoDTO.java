package dataTransferObjects;

import argumentsDTO.CommonEnums.TaskStatus;

public class TaskInfoDTO extends GraphInfoDTO {
    private int enlistedWorkers;
    private TaskStatus taskStatus;
    private boolean enrolled;

    public TaskInfoDTO(int totalNumberOfTargets, int totalNumberOfLeaves, int totalNumberOfMiddles,
                       int totalNumberOfRoots, int totalNumberOfIndependents, int enlistedWorkers,
                       TaskStatus taskStatus, boolean enrolled) {
        super(totalNumberOfTargets, totalNumberOfLeaves, totalNumberOfMiddles,
                totalNumberOfRoots, totalNumberOfIndependents);

        this.enlistedWorkers = enlistedWorkers;
        this.taskStatus = taskStatus;
        this.enrolled = enrolled;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    public void setEnlistedWorkers(int enlistedWorkers) {
        this.enlistedWorkers = enlistedWorkers;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public int getEnlistedWorkers() {
        return enlistedWorkers;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }
}
