package dataTransferObjects;

import javafx.scene.control.ProgressBar;

public class WorkerTaskInfoDto {
    public String taskName;
    public Double taskProgress;
    public Integer enrolledUsers;
    public Integer totalCreditsEarnedByUser;
    public int targetPrice;
    public int targetsIWorkedOn;

    public int getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(int targetPrice) {
        this.targetPrice = targetPrice;
    }

    public int getTargetsIWorkedOn() {
        return targetsIWorkedOn;
    }

    public void setTargetsIWorkedOn(int targetsIWorkedOn) {
        this.targetsIWorkedOn = targetsIWorkedOn;
    }

    public String getTaskName() {
        return taskName;
    }

    public ProgressBar getTaskProgressBar() {
        ProgressBar progressBar = new ProgressBar(taskProgress);
        progressBar.setPrefWidth(600);
        return progressBar;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getEnrolledUsers() {
        return enrolledUsers;
    }

    public void setEnrolledUsers(Integer enrolledUsers) {
        this.enrolledUsers = enrolledUsers;
    }

    public Double getTaskProgress() {
        return taskProgress;
    }

    public void setTaskProgress(Double taskProgress) {
        this.taskProgress = taskProgress;
    }

    public Integer getTotalCreditsEarnedByUser() {
        return totalCreditsEarnedByUser;
    }

    public void setTotalCreditsEarnedByUser(Integer totalCreditsEarnedByUser) {
        this.totalCreditsEarnedByUser = totalCreditsEarnedByUser;
    }
}
