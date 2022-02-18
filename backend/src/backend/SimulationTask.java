package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Random;
import java.util.function.Consumer;

public class SimulationTask extends Task implements Serializable {

    private int msToRun;
    private boolean isRandom;
    private double successRate;
    private double successfulWithWarningRate;
    Random random;

    public SimulationTask(TaskArgs taskArgs, GraphManager graphManager,
                          Consumer<accumulatorForWritingToFile> finishedTargetLog,
                          Consumer<ProgressDto> finishedTarget) {
        super(false, taskArgs.getNumOfThreads(), graphManager
                , finishedTargetLog, finishedTarget);
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
        this.random = new Random();
        this.isRandom = simulationArgs.isRandom();
        this.msToRun = simulationArgs.getSleepTime();
        this.successRate = simulationArgs.getSuccessRate();
        this.successfulWithWarningRate = simulationArgs.getWarningRate();

        graph.values().forEach(target -> {
            target.isRandom = simulationArgs.isRandom();
            target.msToRun = simulationArgs.getSleepTime();
            target.taskName = simulationArgs.getTaskName();
            target.workerName = simulationArgs.getTaskOwner();
            target.successRate = simulationArgs.getSuccessRate();
            target.successfulWithWarningRate = simulationArgs.getWarningRate();
        });
    }


    @Override
    void updateMembersAccordingToTask(TaskArgs taskArgs) {
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;

        this.isRandom = simulationArgs.isRandom();
        this.msToRun = simulationArgs.getSleepTime();
        this.successRate = simulationArgs.getSuccessRate();
        this.successfulWithWarningRate = simulationArgs.getWarningRate();
    }

    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
    @Override
    protected void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        resOfTargetTaskRun.outPutData.add(0, "1. task started, running on - " + targetToExecute.name);
        resOfTargetTaskRun.outPutData.add(1, " * task was ran by: " + targetToExecute.workerName);
        resOfTargetTaskRun.outPutData.add(2, "2. text on target - " +
                (targetToExecute.userData.isEmpty() ? "no text" : targetToExecute.userData));

        updateAccordingToState(targetToExecute, resOfTargetTaskRun);

        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.targetName = targetToExecute.name;

        invokeConsumer(targetToExecute, resOfTargetTaskRun);
        writeTargetResultsToLogFile(resOfTargetTaskRun);
        logData.add(resOfTargetTaskRun);
    }
    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
}
