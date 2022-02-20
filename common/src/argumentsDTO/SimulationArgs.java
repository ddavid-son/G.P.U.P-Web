package argumentsDTO;

import argumentsDTO.CommonEnums.*;

public class SimulationArgs extends TaskArgs {
    private final double successRate;
    private final double warningRate;
    private final int sleepTime;
    private final boolean isRandom;

    public SimulationArgs(double successRate, double warningRate, int sleepTime,
                          boolean isRandom, boolean isWhatIf, RelationType relationType) {
        super(isWhatIf, TaskType.SIMULATION, relationType);
        this.successRate = successRate;
        this.warningRate = warningRate;
        this.sleepTime = sleepTime;
        this.isRandom = isRandom;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public double getWarningRate() {
        return warningRate;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public boolean isRandom() {
        return isRandom;
    }

    @Override
    public TaskArgs cloneArgs() {
        SimulationArgs s = new SimulationArgs(successRate, warningRate, sleepTime, isRandom, isWhatIf, relationType);
        s.taskName = taskName;
        s.taskOwner = taskOwner;
        s.originalGraph = originalGraph;
        s.targetsSelectedForGraph.addAll(targetsSelectedForGraph);
        return s;
    }
}
