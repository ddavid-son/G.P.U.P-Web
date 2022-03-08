package dataTransferObjects;

import argumentsDTO.CommonEnums.*;

import java.util.ArrayList;
import java.util.List;

public class TaskInfoDTO {
    private String taskName;
    private String taskOwner;
    private TaskType taskType;
    private boolean isEnrolled;

    private TaskStatus taskStatus;
    private List<String> enrolledWorker;

    private int independentsCount;
    private int leafsCount;
    private int totalCount;
    private int middlesCount;
    private int rootsCount;

    private int pricePerTarget;

    public TaskInfoDTO(Builder builder) {
        this.taskName = builder.taskName;
        this.taskOwner = builder.taskOwner;
        this.taskType = builder.taskType;
        this.isEnrolled = builder.isEnrolled;
        this.taskStatus = builder.taskStatus;
        this.enrolledWorker = builder.enrolledWorker;
        this.independentsCount = builder.independentsCount;
        this.leafsCount = builder.leafsCount;
        this.totalCount = builder.totalCount;
        this.middlesCount = builder.middlesCount;
        this.rootsCount = builder.rootsCount;
        this.pricePerTarget = builder.pricePerTarget;
    }

    public int numberOfEnrolledWorkers() {
        return enrolledWorker.size();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public void setEnrolled(String worker) {
        isEnrolled = enrolledWorker.contains(worker);
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public int getNumberOfListedWorker() {
        return enrolledWorker.size();
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<String> getEnrolledWorker() {
        return enrolledWorker;
    }

    public void setEnrolledWorker(List<String> enrolledWorker) {
        this.enrolledWorker = enrolledWorker;
    }

    public int getIndependentsCount() {
        return independentsCount;
    }

    public void setIndependentsCount(int independentsCount) {
        this.independentsCount = independentsCount;
    }

    public int getLeafsCount() {
        return leafsCount;
    }

    public void setLeafsCount(int leafsCount) {
        this.leafsCount = leafsCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getMiddlesCount() {
        return middlesCount;
    }

    public void setMiddlesCount(int middlesCount) {
        this.middlesCount = middlesCount;
    }

    public int getRootsCount() {
        return rootsCount;
    }

    public void setRootsCount(int rootsCount) {
        this.rootsCount = rootsCount;
    }

    public int getPricePerTarget() {
        return pricePerTarget;
    }

    public void setPricePerTarget(int pricePerTarget) {
        this.pricePerTarget = pricePerTarget;
    }

    public static class Builder {
        private String taskName;
        private String taskOwner;
        private TaskType taskType;
        private boolean isEnrolled;

        private TaskStatus taskStatus;
        private List<String> enrolledWorker = new ArrayList<>();

        private int independentsCount;
        private int leafsCount;
        private int totalCount;
        private int middlesCount;
        private int rootsCount;

        private int pricePerTarget;

        public TaskInfoDTO build() {
            return new TaskInfoDTO(this);
        }

        public Builder setTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder setTaskOwner(String taskOwner) {
            this.taskOwner = taskOwner;
            return this;
        }

        public Builder setTaskType(TaskType taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder setIsEnrolled(boolean isEnrolled) {
            this.isEnrolled = isEnrolled;
            return this;
        }

        public Builder setTaskStatus(TaskStatus taskStatus) {
            this.taskStatus = taskStatus;
            return this;
        }

        public Builder setEnrolledWorker(List<String> enrolledWorker) {
            this.enrolledWorker = enrolledWorker;
            return this;
        }

        public Builder setIndependentsCount(int independentsCount) {
            this.independentsCount = independentsCount;
            return this;
        }

        public Builder setLeafsCount(int leafsCount) {
            this.leafsCount = leafsCount;
            return this;
        }

        public Builder setTotalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder setMiddlesCount(int middlesCount) {
            this.middlesCount = middlesCount;
            return this;
        }

        public Builder setRootsCount(int rootsCount) {
            this.rootsCount = rootsCount;
            return this;
        }

        public Builder setPricePerTarget(int pricePerTarget) {
            this.pricePerTarget = pricePerTarget;
            return this;
        }

        public Builder setTargetDistribution(GraphInfoDTO graphInfoDTO) {
            this.independentsCount = graphInfoDTO.getTotalNumberOfIndependents();
            this.leafsCount = graphInfoDTO.getTotalNumberOfLeaves();
            this.totalCount = graphInfoDTO.getTotalNumberOfTargets();
            this.middlesCount = graphInfoDTO.getTotalNumberOfMiddles();
            this.rootsCount = graphInfoDTO.getTotalNumberOfRoots();
            return this;
        }
    }
}
