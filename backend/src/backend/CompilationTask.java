package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;

import java.util.function.Consumer;

public class CompilationTask extends Task {

    //private String srcFolderPath;
    //private String dstFolderPath;


    CompilationTask(TaskArgs taskArgs, GraphManager graphManager, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                    Consumer<ProgressDto> finishedTarget) {
        super(false, graphManager,
                finishedTargetLog, finishedTarget);
        CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
        // this.srcFolderPath = compilationArgs.getSrcPath();
        //this.dstFolderPath = compilationArgs.getDstPath();

        graph.values().forEach(target -> {
            target.taskName = compilationArgs.getTaskName();
            target.setTaskType(compilationArgs.getTaskType());
            target.workerName = compilationArgs.getTaskOwner();
            target.srcFolderPath = compilationArgs.getSrcPath();
            target.dstFolderPath = compilationArgs.getDstPath();
        });
    }

    @Override
    void updateMembersAccordingToTask(TaskArgs taskArgs) {
        CompilationArgs simulationArgs = (CompilationArgs) taskArgs;
        //this.srcFolderPath = simulationArgs.getSrcPath();
        //this.dstFolderPath = simulationArgs.getDstPath();
    }

    // ---------------------------- includes internal logic specific to CompilationTask -----------------------------  //
    @Override
    protected void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {

        updateAccordingToState(targetToExecute, resOfTargetTaskRun);
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
