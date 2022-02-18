package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import backend.serialSets.SerialSetManger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.function.Consumer;

public class CompilationTask extends Task {

    private String srcFolderPath;
    private String dstFolderPath;


    CompilationTask(TaskArgs taskArgs, GraphManager graphManager, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                    Consumer<ProgressDto> finishedTarget) {
        super(false, taskArgs.getNumOfThreads(), graphManager,
                finishedTargetLog, finishedTarget);
        CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
        this.srcFolderPath = compilationArgs.getSrcPath();
        this.dstFolderPath = compilationArgs.getDstPath();
    }

    @Override
    void updateMembersAccordingToTask(TaskArgs taskArgs) {
        CompilationArgs simulationArgs = (CompilationArgs) taskArgs;
        this.srcFolderPath = simulationArgs.getSrcPath();
        this.dstFolderPath = simulationArgs.getDstPath();
    }

    // ---------------------------- includes internal logic specific to CompilationTask -----------------------------  //
    @Override
    protected void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {

        if (targetToExecute.state == TargetState.SUCCESS)
            removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
        else {
            notifyAllAncestorToBeSkipped(targetToExecute, resOfTargetTaskRun);
            updateOpenTargets(targetToExecute, resOfTargetTaskRun);
        }
        updateAccumulator(resOfTargetTaskRun, targetToExecute);

        writeTargetResultsToLogFile(resOfTargetTaskRun);
        logData.add(resOfTargetTaskRun);
    }

    private void updateAccumulator(accumulatorForWritingToFile resOfTargetTaskRun, TaskTarget targetToExecute) {

        resOfTargetTaskRun.outPutData.add("* Targets opened : " + resOfTargetTaskRun.targetOpened);
        if (targetToExecute.state == TargetState.FAILURE) {
            resOfTargetTaskRun.outPutData.add("* Target skipped due to failure: " + resOfTargetTaskRun.SkippedTargets);
        }
    }
    // ---------------------------- includes internal logic specific to CompilationTask -----------------------------  //
}
