package argumentsDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import argumentsDTO.CommonEnums.*;

public class TaskTarget implements Serializable {

    public String taskName;
    public String userData;
    public TargetType type;
    public TargetState state;
    public final String name;
    public String workerName;
    public TaskType taskType;

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public long enterWaiting = 0;
    public long enterProcess = 0;
    public List<String> dependsOn;
    public List<String> requiredFor;
    public final List<String> nameOfFailedOrSkippedDependencies = new ArrayList<>();

    // simulation
    public int msToRun;
    public boolean isRandom;
    public double successRate;
    public double successfulWithWarningRate;

    //compilation
    public String srcFolderPath;
    public String dstFolderPath;


    public TaskTarget(String name, TargetType type, String userData, String taskName) {

        this.type = type;
        this.name = name;
        this.userData = userData;
        this.taskName = taskName;
        if (type == TargetType.INDEPENDENT || type == TargetType.LEAF) {
            state = TargetState.WAITING;
            enterWaiting = System.currentTimeMillis();
        } else {
            this.state = TargetState.FROZEN;
        }
    }

    //ctor

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public void setRequiredFor(List<String> requiredFor) {
        this.requiredFor = requiredFor;
    }
  /*  public TaskTarget(Target target) {
        this.name = target.getName();
        this.type = target.getType();
        this.userData = target.getUserData();
        if (type == CommonEnums.TargetType.INDEPENDENT || type == CommonEnums.TargetType.LEAF) {
            state = CommonEnums.TargetState.WAITING;
            waitingList.add(name);
            enterWaiting = System.currentTimeMillis();
        } else {
            this.state = CommonEnums.TargetState.FROZEN;
        }

        this.dependsOn = target.getDependsOnNames();
        this.requiredFor = target.getRequiredForNames();
        this.serialSetsName.addAll(target.getSerialSetsName());
    }*/

    public String getName() {
        return this.name;
    }

    public String getUserData() {
        return this.userData;
    }

    public TargetType getType() {
        return this.type;
    }

    public TargetState getState() {
        return this.state;
    }

    public List<String> getDependsOnNames() {
        return this.dependsOn;
    }

    public List<String> getRequiredForNames() {
        return this.requiredFor;
    }

    public List<String> getNameOfFailedOrSkippedDependencies() {
        return this.nameOfFailedOrSkippedDependencies;
    }

    @Override
    public String toString() {
        return "TaskTarget{" +
                "type=" + type +
                ", state=" + state +
                ", name='" + name + '\'' +
                ", workerName='" + workerName + '\'' +
                ", taskName='" + taskName + '\'' +
                ", userData='" + userData + '\'' +
                ", enterWaiting=" + enterWaiting +
                ", enterProcess=" + enterProcess +
                ", dependsOn=" + dependsOn +
                ", requiredFor=" + requiredFor +
                ", nameOfFailedOrSkippedDependencies=" + nameOfFailedOrSkippedDependencies +
                ", msToRun=" + msToRun +
                ", isRandom=" + isRandom +
                ", successRate=" + successRate +
                ", successfulWithWarningRate=" + successfulWithWarningRate +
                ", srcFolderPath='" + srcFolderPath + '\'' +
                ", dstFolderPath='" + dstFolderPath + '\'' +
                '}';
    }
}