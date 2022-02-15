package argumentsDTO;

import java.util.ArrayList;
import java.util.List;

import argumentsDTO.CommonEnums.*;


public abstract class TaskArgs {

    boolean isWhatIf;
    int numOfThreads;
    TaskType taskType;
    boolean isIncremental;
    RelationType relationType;
    final List<String> targetsSelectedForGraph = new ArrayList<>();

    String taskName;
    String taskOwner;
    String originalGraph;

    public TaskArgs() {
    }


    public TaskArgs(boolean isWhatIf, int numOfThreads, TaskType taskType, boolean isIncremental,
                    RelationType relationType) {
        this.isWhatIf = isWhatIf;
        this.taskType = taskType;
        this.relationType = relationType;
        this.numOfThreads = numOfThreads;
        this.isIncremental = isIncremental;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public boolean isIncremental() {

        return isIncremental;
    }

    public boolean isWhatIf() {

        return isWhatIf;
    }

    public int getNumOfThreads() {

        return numOfThreads;
    }

    public TaskType getTaskType() {

        return taskType;
    }

    public List<String> getTargetsSelectedForGraph() {

        return targetsSelectedForGraph;
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

    public String getOriginalGraph() {

        return originalGraph;
    }

    public void setOriginalGraph(String originalGraph) {

        this.originalGraph = originalGraph;
    }

    @Override
    public String toString() {
        return "taskArgs: \n" +
                "taskName=" + taskName + "\n" +
                "taskOwner=" + taskOwner + "\n" +
                "originalGraph=" + originalGraph + "\n" +
                "taskType=" + taskType + "\n" +
                "numOfThreads=" + numOfThreads + "\n" +
                "isWhatIf=" + isWhatIf + "\n" +
                "isIncremental=" + isIncremental + "\n" +
                "relationType=" + relationType + "\n" +
                "targetsSelectedForGraph=" + targetsSelectedForGraph;
    }

}
