package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import backend.serialSets.SerialSetManger;
import dataTransferObjects.UpdateListsDTO;
import javafx.application.Platform;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Task implements Serializable {

    int outOfWaiting = 0;
    long startTime;
    String taskName;
    String serverFullPath;
    int numberOfFinishedTargets = 0;
    boolean allGraphHasBeenProcessed;
    Consumer<ProgressDto> finishedTarget;
    List<String> waitingList = new LinkedList<>();
    Map<String, TaskTarget> graph = new HashMap<>();
    Consumer<accumulatorForWritingToFile> finishedTargetLog;
    final String WORKING_DIR = "c:\\gpup-working-dir";
    List<accumulatorForWritingToFile> logData = new LinkedList<>();

    public Set<String> frozenSet = new HashSet<>();
    public Set<String> warningSet = new HashSet<>();
    public Set<String> skippedSet = new HashSet<>();
    public Set<String> SuccessSet = new HashSet<>();
    public Set<String> inProcessSet = new HashSet<>();
    public Set<String> waitingSet = new HashSet<>();
    public Set<String> failedSet = new HashSet<>();
    public UpdateListsDTO updateListsDTO = new UpdateListsDTO();

    //probably will be removed
    int numberOfThreads;
    ThreadPoolExecutor threadPool;
    private int numberOfThreadActive = 0;


    public void pauseTask() {
    }

    public void resumeTask() {

    }

    private void pauseThreadTask() {

    }

    public UpdateListsDTO getUpdateListsDTO() {
        updateListsDTO.setAll(
                frozenSet, warningSet, skippedSet, SuccessSet,
                inProcessSet, waitingSet, failedSet);

        return updateListsDTO;
    }

    private synchronized void updateNumberOfActiveThreads(boolean isUp) {
        outOfWaiting += isUp ? 1 : 0;
        numberOfThreadActive = isUp ? numberOfThreadActive + 1 : numberOfThreadActive - 1;
        int idle = numberOfThreads - numberOfThreadActive;
        int waiting = waitingList.size() - outOfWaiting;
        Platform.runLater(() -> finishedTargetLog.accept(new accumulatorForWritingToFile("\n" +
                TimeUtil.ltn(System.currentTimeMillis()) + " there are currently " + idle + " idle threads. " +
                "still in Queue: " + waiting + "\n")));
    }

    // ---------------------------------------------- ctor and utils ------------------------------------------------ //
    public Task(boolean allGraphHasBeenProcessed, int numberOfThreads, GraphManager graphManager,
                Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        this.numberOfThreads = numberOfThreads;
        this.allGraphHasBeenProcessed = allGraphHasBeenProcessed;
        setConsumers(finishedTargetLog, finishedTarget);
        buildTaskGraph(graphManager);
    }

    protected void buildTaskGraph(GraphManager graphManager) {
        for (Target target : graphManager.getTargetArray()) {

            TaskTarget taskTarget = new TaskTarget(target.getName(), target.getType(), target.getUserData(), taskName);
            taskTarget.setDependsOn(target.getDependsOnNames());
            taskTarget.setRequiredFor(target.getRequiredForNames());

            if (target.getState() == TargetState.WAITING) {
                waitingList.add(target.getName());
                waitingSet.add(target.getName());
            } else {
                frozenSet.add(target.getName());
            }
            graph.put(target.getName(), taskTarget);
        }
    }

    protected synchronized void incrementFinishedThreadsCount() {

        numberOfFinishedTargets++;
    }

    boolean getAllGraphHasBeenProcessed() {

        return allGraphHasBeenProcessed;
    }
    // ---------------------------------------------- ctor and utils ------------------------------------------------ //


    // --------------------------------------------------- run ------------------------------------------------------ //
    public void activateRun() {
        startTime = System.currentTimeMillis();
        serverFullPath = createDirectoryToLogData(startTime);
    }

    public boolean isTaskFinished() {
        return waitingList.isEmpty();
    }

    public TaskTarget getWork() {
        if (waitingList.size() == 0)
            return null;
        TaskTarget targetToExecute = graph.get(waitingList.remove(0));
        targetToExecute.state = TargetState.IN_PROCESS;
        targetToExecute.enterProcess = System.currentTimeMillis();

        waitingSet.remove(targetToExecute.name);
        inProcessSet.add(targetToExecute.name);// consider saving to which worker it was assigned
        return targetToExecute;
    }

    public void giveResults(TaskTarget targetToExecute) {
        // switch on result

        //
    }

    public long getWaitingStartTime(String targetName) {
        return graph.get(targetName).enterWaiting;
    }

    public long getProcessStartTime(String targetName) {
        return graph.get(targetName).enterProcess;
    }

    public List<String> getFailedOrSkipped(String targetName) {
        return graph.get(targetName).nameOfFailedOrSkippedDependencies;
    }

    public ProgressDto getInfoAboutTargetInExecution(String targetName) {
        return new ProgressDto(
                "",
                graph.get(targetName).name,
                graph.get(targetName).state,
                graph.get(targetName).enterWaiting,
                graph.get(targetName).enterProcess,
                String.join(",", graph.get(targetName).nameOfFailedOrSkippedDependencies),
                String.join(",", graph.get(targetName).dependsOn)
        );
    }

    private void printRunSummary(Consumer<String> print, long graphRunStartTime) {
        long graphRunEndTime = System.currentTimeMillis();
        print.accept("Simulation finished in " +
                (graphRunEndTime - graphRunStartTime) / 1000 +
                "." + (graphRunEndTime - graphRunStartTime) % 1000 +
                " s");
        //simulationRunSummary(print);
    }

    private void sendToNewThreadAndPushToPool(/*String fullPath, */TaskTarget targetToExecute, accumulatorForWritingToFile finalResOfTargetTaskRun) {
        Thread t = new Thread(() -> {
            //pauseThreadTask();
            //updateNumberOfActiveThreads(true);
            //runTaskOnTarget(targetToExecute, finalResOfTargetTaskRun);
            writeTargetResultsToLogFile(finalResOfTargetTaskRun /*,fullPath*/);
            logData.add(finalResOfTargetTaskRun);
            incrementFinishedThreadsCount();
            //updateNumberOfActiveThreads(false);
        }, "thread #: " + numberOfFinishedTargets);
        threadPool.execute(t);
    }

    private String getNamesToRunLater(TaskTarget targetToExecute, accumulatorForWritingToFile finalResOfTargetTaskRun) {
        if (targetToExecute.state == TargetState.FAILURE) {
            return targetToExecute.name + "," + String.join(",", finalResOfTargetTaskRun.SkippedTargets);
        }
        return targetToExecute.name;
    }

    private void setConsumers(Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        if (this.finishedTargetLog == null && this.finishedTarget == null) {
            this.finishedTargetLog = finishedTargetLog;
            this.finishedTarget = finishedTarget;
        }
    }

    protected void targetSummary(accumulatorForWritingToFile resOfTargetTaskRun, Consumer<String> print) {
        print.accept("");
        for (String lineInSummary : resOfTargetTaskRun.outPutData) {
            print.accept(lineInSummary);
        }
        print.accept("");
    }

    protected void simulationRunSummary(Consumer<String> print) {

        int skipped = 0, Failed = 0, warning = 0, success = 0;

        //count all targets that participated in the simulation with results
        for (accumulatorForWritingToFile res : logData) {
            if (res.targetState == TargetState.FAILURE) Failed++;
            if (res.targetState == TargetState.WARNING) warning++;
            if (res.targetState == TargetState.SUCCESS) success++;
        }
        //count all targets that didn't participated in the simulation i.e . they were skipped
        for (TaskTarget target : graph.values()) // can be a simple subtraction operation... why I did it this way ?
            if (target.state == TargetState.SKIPPED) skipped++;

        if (skipped == 0 && Failed == 0)
            allGraphHasBeenProcessed = true; // todo: need to consider if this is true in case of Circle in Graph

        print.accept("number of Skipped targets: " + skipped +
                "\nnumber of Failed targets: " + Failed +
                "\nnumber of warning targets: " + warning +
                "\nnumber of success targets: " + success + "\n");

        logData.forEach(data -> {
            print.accept(data.targetName +
                    "\nstate: " + data.targetState);

            logData.forEach(target -> {
                if (data.targetName.equals(target.targetName))
                    print.accept("task ran took " + TimeUtil.ltd(target.totalTimeToRun) + "\n");

            });

            data.SkippedTargets.forEach(target -> print.accept(target +
                    "\nstate: SKIPPED\n" +
                    "task ran took 00:00:00.000 \n"));
        });
    }

    protected String createDirectoryToLogData(long graphRunStartTime) {
        Date d = new Date(graphRunStartTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        File logFile = new File(WORKING_DIR + "\\" + (this instanceof SimulationTask ? "simulation - " : "compilation - ") + sdf.format(d));
        if (!logFile.exists()) {
            try {
                if (!logFile.mkdirs()) throw new IOException("Could not create directory");
            } catch (SecurityException | IOException se) {
                throw new IllegalArgumentException("could not find the folder and creating it failed too" +
                        "(could be due to permission issues), please choose another directory");
            }
        }
        return logFile.getAbsolutePath();
    }

    protected void writeTargetResultsToLogFile(accumulatorForWritingToFile resOfTargetTaskRun/*, String fullPath*/) {
        //File logFile = new File(fullPath + "\\" + resOfTargetTaskRun.targetName + ".log");
        File logFile = new File(serverFullPath + ".log");

        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot create log file");
        }

        try {
            FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (String str : resOfTargetTaskRun.outPutData) {
                try {
                    bw.write(str + '\n');
                } catch (Exception e) {/**/}
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot write to log file " + e.getMessage());
        }
    }

    protected synchronized void updateOpenTargets(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        for (String father : targetToExecute.requiredFor) {
            graph.get(father).dependsOn.remove(targetToExecute.name);
            if (graph.get(father).dependsOn.isEmpty())
                resOfTargetTaskRun.targetOpened.add(father);
        }
    }

    protected void invokeConsumer(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        resOfTargetTaskRun.outPutData.add(
                "3. task finished running on - " + targetToExecute.name + " task results: " + targetToExecute.state);

        resOfTargetTaskRun.outPutData.add(
                "4. targets opened to execution by this operation:\n" + (resOfTargetTaskRun.targetOpened.isEmpty() ?
                        "no targets opened" : resOfTargetTaskRun.targetOpened));

        if (targetToExecute.state.equals(TargetState.FAILURE)) {
            resOfTargetTaskRun.outPutData.add("5. targets that will be skipped due to: " + targetToExecute.name + "'s failure:\n" +
                    (resOfTargetTaskRun.SkippedTargets.isEmpty() ? "no targets skipped" : resOfTargetTaskRun.SkippedTargets));
        }
    }

    protected synchronized void removeAndUpdateDependenciesAfterSuccess(TaskTarget targetToExecute,
                                                                        accumulatorForWritingToFile resOfTargetTaskRun) {

        targetToExecute.requiredFor.forEach(neighbour -> {
            graph.get(neighbour).dependsOn.remove(targetToExecute.name);
            if (graph.get(neighbour).dependsOn.isEmpty()) {
                if (!graph.get(neighbour).state.equals(TargetState.SKIPPED) &&
                        !waitingList.contains(neighbour)) {
                    //Platform.runLater(() -> finishedTarget.accept(new ProgressDto(neighbour, TargetState.WAITING)));
                    waitingList.add(neighbour);
                    graph.get(neighbour).state = TargetState.WAITING;
                }
                resOfTargetTaskRun.targetOpened.add(neighbour);
            }
        });
    }

    protected synchronized void notifyAllAncestorToBeSkipped(TaskTarget targetToExecute,
                                                             accumulatorForWritingToFile resOfTargetTaskRun) {
        if (!targetToExecute.state.equals(TargetState.SKIPPED)) {
            for (String ancestor : targetToExecute.requiredFor) {
                if (!graph.get(ancestor).state.equals(TargetState.SKIPPED)) {
                    if (!resOfTargetTaskRun.SkippedTargets.contains(ancestor))
                        resOfTargetTaskRun.SkippedTargets.add(ancestor);
                    notifyAllAncestorToBeSkipped(graph.get(ancestor), resOfTargetTaskRun);
                    graph.get(ancestor).state = TargetState.SKIPPED;
                    graph.get(ancestor).nameOfFailedOrSkippedDependencies.add(targetToExecute.name);
                }
            }
        }
    }
    // --------------------------------------------------- run ------------------------------------------------------ //


    // ------------------------------------------------- getReady --------------------------------------------------- //
    public void getReadyForIncrementalRun(TaskArgs taskArgs) {

        updateMembersAccordingToTask(taskArgs);
        numberOfThreads = taskArgs.getNumOfThreads();

        // remove all succeeded targets from waiting list
        waitingList = waitingList.stream()
                .filter(targetName -> graph.get(targetName).state == TargetState.FAILURE)
                .collect(Collectors.toCollection(LinkedList::new));

        waitingList.forEach(targetName -> {
            graph.get(targetName).requiredFor.forEach(reqName ->
                    graph.get(reqName).dependsOn.add(targetName));
            graph.get(targetName).state = TargetState.WAITING;
            //Platform.runLater(() -> finishedTarget.accept(new ProgressDto(targetName, TargetState.WAITING)));
        });

        // RESETTING ALL SKIPPED TARGET TO THEIR ACTUAL STATE
        for (TaskTarget target : graph.values()) {
            if (target.state == TargetState.SKIPPED) {
                target.nameOfFailedOrSkippedDependencies.forEach(skippDep -> {
                    if (!target.dependsOn.contains(skippDep))
                        graph.get(skippDep).dependsOn.add(target.name);
                });
                target.state = TargetState.FROZEN;
                target.nameOfFailedOrSkippedDependencies.clear();
            }
        }

        logData.clear();
    }
    // ------------------------------------------------- getReady --------------------------------------------------- //


    // ----------------------------------------- abstract Methods --------------------------------------------------- //
    abstract void updateMembersAccordingToTask(TaskArgs taskArgs);

    abstract void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun);

    // ----------------------------------------- abstract Methods --------------------------------------------------- //

}
