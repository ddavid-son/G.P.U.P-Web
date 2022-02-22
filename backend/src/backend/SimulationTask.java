package backend;

import argumentsDTO.*;


import java.io.Serializable;
import java.util.function.Consumer;

public class SimulationTask extends Task implements Serializable {

    public SimulationTask(TaskArgs taskArgs, GraphManager graphManager,
                          Consumer<accumulatorForWritingToFile> finishedTargetLog,
                          Consumer<ProgressDto> finishedTarget) {
        super(false, graphManager
                , finishedTargetLog, finishedTarget);
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;

        graph.values().forEach(target -> {
            target.isRandom = simulationArgs.isRandom();
            target.msToRun = simulationArgs.getSleepTime();
            target.taskName = simulationArgs.getTaskName();
            target.setTaskType(simulationArgs.getTaskType());
            target.workerName = simulationArgs.getTaskOwner();
            target.successRate = simulationArgs.getSuccessRate();
            target.successfulWithWarningRate = simulationArgs.getWarningRate();
        });
    }


    @Override
    void updateMembersAccordingToTask(TaskArgs taskArgs) {
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
    }

    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
    @Override
    protected void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        resOfTargetTaskRun.outPutData.add(0, "1. task started, running on - " + targetToExecute.name);
        resOfTargetTaskRun.outPutData.add(1, " * task owner is: " + targetToExecute.workerName);
        resOfTargetTaskRun.outPutData.add(2, "2. text on target - " +
                (targetToExecute.userData.isEmpty() ? "no text" : targetToExecute.userData));

        updateAccordingToState(targetToExecute, resOfTargetTaskRun);

        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.targetName = targetToExecute.name;

        invokeConsumer(targetToExecute, resOfTargetTaskRun);
        writeTargetResultsToLogFile(resOfTargetTaskRun);
        synchronized (lockForLogsUpdate) {
            logData.add(resOfTargetTaskRun);
        }
    }
    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
}
