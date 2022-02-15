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

 /*   public TaskArgs(TaskArgsBuilder taskArgsBuilder) {
        this.isWhatIf = taskArgsBuilder.isWhatIf;
        this.numOfThreads = taskArgsBuilder.numOfThreads;
        this.taskType = taskArgsBuilder.taskType;
        this.isIncremental = taskArgsBuilder.isIncremental;
        this.relationType = taskArgsBuilder.relationType;
        this.taskName = taskArgsBuilder.taskName;
        this.taskOwner = taskArgsBuilder.taskOwner;
        this.originalGraph = taskArgsBuilder.originalGraph;
    }*/

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
        return "TaskArgs [isWhatIf=" + isWhatIf + ", numOfThreads=" + numOfThreads + ", taskType=" + taskType
                + ", isIncremental=" + isIncremental + ", relationType=" + relationType + ", targetsSelectedForGraph="
                + targetsSelectedForGraph + ", taskName=" + taskName + ", taskOwner=" + taskOwner + ", originalGraph="
                + originalGraph + "]";
    }

   /* public static class TaskArgsBuilder {

        private boolean isWhatIf;
        private int numOfThreads;
        private TaskType taskType;
        private boolean isIncremental;
        private RelationType relationType;
        private List<String> targetsSelectedForGraph = new ArrayList<>();
        private String taskName;
        private String taskOwner;
        private String originalGraph;

        public TaskArgsBuilder setTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public TaskArgsBuilder setTaskOwner(String taskOwner) {
            this.taskOwner = taskOwner;
            return this;
        }

        public TaskArgsBuilder setOriginalGraph(String originalGraph) {
            this.originalGraph = originalGraph;
            return this;
        }

        public TaskArgsBuilder setIsWhatIf(boolean isWhatIf) {
            this.isWhatIf = isWhatIf;
            return this;
        }

        public TaskArgsBuilder setNumOfThreads(int numOfThreads) {
            this.numOfThreads = numOfThreads;
            return this;
        }

        public TaskArgsBuilder setTaskType(TaskType taskType) {
            this.taskType = taskType;
            return this;
        }

        public TaskArgsBuilder setIsIncremental(boolean isIncremental) {
            this.isIncremental = isIncremental;
            return this;
        }

        public TaskArgsBuilder setRelationType(RelationType relationType) {
            this.relationType = relationType;
            return this;
        }

        public TaskArgsBuilder setTargetsSelectedForGraph(List<String> targetsSelectedForGraph) {
            this.targetsSelectedForGraph = targetsSelectedForGraph;
            return this;
        }

        public TaskArgs build() {
            TaskArgs taskArgs = new TaskArgs(this);
            //validate here
            return taskArgs;
        }
    }*/

}
