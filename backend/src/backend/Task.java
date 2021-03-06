package backend;

import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import dataTransferObjects.UpdateListsDTO;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class Task implements Serializable {

    long startTime;
    String taskName;
    String serverFullPath;
    boolean allGraphHasBeenProcessed;
    Consumer<ProgressDto> finishedTarget;
    private final GraphManager costumeGraph;
    List<String> waitingList = new LinkedList<>();
    final Object lockForUpdateTables = new Object();
    Map<String, TaskTarget> graph = new HashMap<>();
    final String WORKING_DIR = "c:\\gpup-working-dir";
    Consumer<accumulatorForWritingToFile> finishedTargetLog;
    List<accumulatorForWritingToFile> logData = new LinkedList<>();
    final Object lockForLogsUpdate = new Object();

    public Set<String> frozenSet = new HashSet<>();
    public Set<String> warningSet = new HashSet<>();
    public Set<String> skippedSet = new HashSet<>();
    public Set<String> SuccessSet = new HashSet<>();
    public Set<String> inProcessSet = new HashSet<>();
    public Set<String> waitingSet = new HashSet<>();
    public Set<String> failedSet = new HashSet<>();
    public UpdateListsDTO updateListsDTO = new UpdateListsDTO();

    public List<String> getFailedAndSkippedList() {
        List<String> result = new LinkedList<>();
        result.addAll(failedSet);
        result.addAll(skippedSet);
        return result;
    }

    public UpdateListsDTO getUpdateListsDTO(int listedUsers) {
        synchronized (lockForUpdateTables) {

            updateListsDTO.setAll(
                    frozenSet, warningSet, skippedSet, SuccessSet,
                    inProcessSet, waitingSet, failedSet);

            updateListsDTO.setRegisteredUsers(listedUsers);
            if (allGraphHasBeenProcessed && inProcessSet.isEmpty() && updateListsDTO.endTime == 0) {
                updateListsDTO.startTime = startTime;
                updateListsDTO.endTime = System.currentTimeMillis();
            }
            return updateListsDTO;
        }
    }

    // ---------------------------------------------- ctor and utils ------------------------------------------------ //
    public Task(boolean allGraphHasBeenProcessed, GraphManager graphManager,
                Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        this.costumeGraph = graphManager;
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

    boolean getAllGraphHasBeenProcessed() {

        return allGraphHasBeenProcessed;
    }

    protected void moveTargetsBetweenSets(Set<String> src, Set<String> dst, String targetName) {
        synchronized (lockForUpdateTables) {
            src.remove(targetName);
            dst.add(targetName);
        }
    }

    List<accumulatorForWritingToFile> fetchDeltaLogs(int lastVisitedIndex) {
        return logData.subList(lastVisitedIndex, logData.size());
    }
    // ---------------------------------------------- ctor and utils ------------------------------------------------ //


    // --------------------------------------------------- run ------------------------------------------------------ //
    public void activateRun() {
        startTime = System.currentTimeMillis();
        serverFullPath = createDirectoryToLogData(startTime);
    }

    public TaskTarget getWork() {
        TaskTarget targetToExecute;
        synchronized (this) {
            if (waitingList.size() == 0) {
                if (frozenSet.isEmpty()) {
                    allGraphHasBeenProcessed = true;
                }
                return null;
            }
            targetToExecute = graph.get(waitingList.remove(0));
        }
        targetToExecute.state = TargetState.IN_PROCESS;
        targetToExecute.enterProcess = System.currentTimeMillis();
        moveTargetsBetweenSets(waitingSet, inProcessSet, targetToExecute.name);
        return targetToExecute;
    }

    private void setConsumers(Consumer<accumulatorForWritingToFile> finishedTargetLog, Consumer<ProgressDto> finishedTarget) {
        if (this.finishedTargetLog == null && this.finishedTarget == null) {
            this.finishedTargetLog = finishedTargetLog;
            this.finishedTarget = finishedTarget;
        }
    }

    protected String createDirectoryToLogData(long graphRunStartTime) {
        Date d = new Date(graphRunStartTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        File logFile = new File(WORKING_DIR + "\\" + (this instanceof SimulationTask ?
                "simulation - " : "compilation - ") + sdf.format(d));
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

    protected void writeTargetResultsToLogFile(accumulatorForWritingToFile resOfTargetTaskRun) {
        File logFile = new File(serverFullPath + "\\" + taskName + " - " + resOfTargetTaskRun.targetName + ".log");

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
            if (graph.get(father).dependsOn.isEmpty()) {
                resOfTargetTaskRun.targetOpened.add(father);
            }
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
                    waitingList.add(neighbour);
                    moveTargetsBetweenSets(frozenSet, waitingSet, neighbour);
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
                    moveTargetsBetweenSets(frozenSet, skippedSet, ancestor);
                    graph.get(ancestor).state = TargetState.SKIPPED;
                    graph.get(ancestor).nameOfFailedOrSkippedDependencies.add(targetToExecute.name);
                }
            }
        }
    }

    protected void updateAccordingToState(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun) {
        switch (targetToExecute.state) {
            case SUCCESS:
                moveTargetsBetweenSets(inProcessSet, SuccessSet, targetToExecute.name);
                removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
                break;
            case WARNING:
                moveTargetsBetweenSets(inProcessSet, warningSet, targetToExecute.name);
                removeAndUpdateDependenciesAfterSuccess(targetToExecute, resOfTargetTaskRun);
                break;
            case FAILURE:
                moveTargetsBetweenSets(inProcessSet, failedSet, targetToExecute.name);
                notifyAllAncestorToBeSkipped(targetToExecute, resOfTargetTaskRun);
                updateOpenTargets(targetToExecute, resOfTargetTaskRun);
                break;
        }
    }

    public List<String> getInfoAboutTargetInExecution(String targetName, TargetState targetState) {
        List<String> info = new ArrayList<>();
        info.add(targetName);//0
        info.add(graph.get(targetName).type.toString());//1
        info.add("");//2
        info.add(targetState.toString());//3
        switch (targetState) {
            case WAITING:
                long waiting = System.currentTimeMillis() - graph.get(targetName).enterWaiting;
                info.add(TimeUtil.ltd(waiting));
                break;
            case IN_PROCESS:
                long processing = System.currentTimeMillis() - graph.get(targetName).enterProcess;
                info.add(TimeUtil.ltd(processing));
                break;
            case SKIPPED:
                info.add(costumeGraph.getDependsOnOfByName(targetName).stream().filter(neighbour ->
                        failedSet.contains(neighbour) || skippedSet.contains(neighbour)).collect(Collectors.joining(",")));
                break;
            case FROZEN:
                info.add(costumeGraph.getDependsOnOfByName(targetName).stream().filter(neighbour ->
                        frozenSet.contains(neighbour) || inProcessSet.contains(neighbour) ||
                                waitingSet.contains(neighbour)).collect(Collectors.joining(",")));
                break;
            case SUCCESS:
            case WARNING:
            case FAILURE:
                info.add(targetState.toString());
                break;
        }
        return info;
    }
    // --------------------------------------------------- run ------------------------------------------------------ //


    // ----------------------------------------- abstract Methods --------------------------------------------------- //
    abstract void updateMembersAccordingToTask(TaskArgs taskArgs);

    abstract void finishWorkOnTarget(TaskTarget targetToExecute, accumulatorForWritingToFile resOfTargetTaskRun);

    public synchronized double getProgress() {
        double done = graph.size() - (frozenSet.size() + waitingSet.size() + inProcessSet.size());
        double total = graph.size();
        return done / total;
    }
    // ----------------------------------------- abstract Methods --------------------------------------------------- //
}
