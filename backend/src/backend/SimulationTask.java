package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import backend.serialSets.SerialSetManger;

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
                          SerialSetManger serialSetManager, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                          Consumer<ProgressDto> finishedTarget) {
        super(false, serialSetManager, taskArgs.getNumOfThreads(), graphManager
                , finishedTargetLog, finishedTarget);
        SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
        this.random = new Random();
        this.isRandom = simulationArgs.isRandom();
        this.msToRun = simulationArgs.getSleepTime();
        this.successRate = simulationArgs.getSuccessRate();
        this.successfulWithWarningRate = simulationArgs.getWarningRate();
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
    protected void runTaskOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        resOfTargetTaskRun.outPutData.add("1. task started, running on - " + targetToExecute.name);

        resOfTargetTaskRun.outPutData.add("2. text on target - " +
                (targetToExecute.userData.isEmpty() ? "no text" : targetToExecute.userData));

        performSimulation(resOfTargetTaskRun);

        updateGraphAccordingToTheResults(targetToExecute, resOfTargetTaskRun);
        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.targetName = targetToExecute.name;

        invokeConsumer(targetToExecute, resOfTargetTaskRun);
    }

    private void performSimulation(accumulatorForWritingToFile resOfTargetTaskRun) {
        resOfTargetTaskRun.startTime = System.currentTimeMillis();
        Timestamp ts = new Timestamp(resOfTargetTaskRun.startTime);
        try {
            if (isRandom) {
                int randomNumberToWait = random.nextInt(msToRun);
                resOfTargetTaskRun.outPutData.add(" * task was ran by thread: " + Thread.currentThread().getName());
                resOfTargetTaskRun.outPutData.add("  * going to sleep for " + TimeUtil.ltd(randomNumberToWait));

                resOfTargetTaskRun.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(randomNumberToWait);
            } else {
                resOfTargetTaskRun.outPutData.add("  * going to sleep for " + TimeUtil.ltd(msToRun));

                resOfTargetTaskRun.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(msToRun);
            }
            resOfTargetTaskRun.endTime = System.currentTimeMillis();
            ts.setTime(resOfTargetTaskRun.endTime);

            resOfTargetTaskRun.outPutData.add("  * top of the morning to ya good sir " + ts.toString().substring(10));
            resOfTargetTaskRun.totalTimeToRun = resOfTargetTaskRun.endTime - resOfTargetTaskRun.startTime;

        } catch (InterruptedException e) { /**/ }
        resOfTargetTaskRun.endTime = System.currentTimeMillis();
    }

    private void updateGraphAccordingToTheResults(TaskTarget targetToExecute,
                                                  accumulatorForWritingToFile resOfTargetTaskRun
    ) {
        if (random.nextDouble() <= successRate) {
            targetToExecute.state = TargetState.SUCCESS;
            if (random.nextDouble() <= successfulWithWarningRate) {
                targetToExecute.state = TargetState.WARNING;
            }
            removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
        } else {
            targetToExecute.state = TargetState.FAILURE;
            notifyAllAncestorToBeSkipped(targetToExecute, resOfTargetTaskRun);
            updateOpenTargets(targetToExecute, resOfTargetTaskRun);
        }
    }
    // ---------------------------- includes internal logic specific to simulationTask -----------------------------  //
}
