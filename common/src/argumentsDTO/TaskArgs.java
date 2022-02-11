package argumentsDTO;

import java.util.ArrayList;
import java.util.List;
import argumentsDTO.CommonEnums.*;


public abstract class TaskArgs {

    final boolean isWhatIf;
    final RelationType relationType;
    final int numOfThreads;
    final TaskType taskType;
    final boolean isIncremental;
    final List<String> targetsSelectedForGraph = new ArrayList<>();

    public TaskArgs(boolean isWhatIf, int numOfThreads, TaskType taskType, boolean isIncremental,
                    RelationType relationType) {
        this.isWhatIf = isWhatIf;
        this.taskType = taskType;
        this.numOfThreads = numOfThreads;
        this.isIncremental = isIncremental;
        this.relationType = relationType;
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

}
