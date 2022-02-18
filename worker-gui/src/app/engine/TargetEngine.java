package app.engine;

import app.Utils.FXUtil;
import app.Utils.HttpUtil;
import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import okhttp3.internal.concurrent.TaskLoggerKt;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

public class TargetEngine {

    private final int poolSize;
    private boolean pauseFetching;
    private int activeThreads = 0;
    private final String workerName;
    private final ThreadPoolExecutor pool;
    private Timer timerForFetching;
    private Timer timerForPushing;
    private final Random random = new Random();

    private int lastPushedIndex = 0;
    private final List<String> tasksImIn = new ArrayList<>();
    private final List<TaskTarget> finishedWork = new ArrayList<>();
    private final List<accumulatorForWritingToFile> finishedLogs = new ArrayList<>();

    //private final List<TaskTarget> finishedWorkHistory = new ArrayList<>();
    //private final List<accumulatorForWritingToFile> finishedLogsHistory = new ArrayList<>();

    // ----------------------------------------- server call management ----------------------------------------------//
    // fetching schedule
    public void scheduleTargetFetching() {
        timerForFetching.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchTargetsFromServet();
            }
        }, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    private void fetchTargetsFromServet() {
        if (!pauseFetching) {
            String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/fetch-work")
                    .newBuilder()
                    .addQueryParameter("username", workerName)
                    .addQueryParameter("available-resources", String.valueOf(getFreeThreads()))
                    .build()
                    .toString();

            HttpUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    FXUtil.handleErrors(e, "", "Error! couldn't fetch work from server");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String res = response.body().string();
                    if (response.isSuccessful()) {
                        List<TaskTarget> work = HttpUtil.GSON.fromJson(res, new TypeToken<List<TaskTarget>>() {
                        }.getType());
                        updateNumberOfActiveThreads(work.size());
                        addTasksToQueue(work, "SIMULATION");
                    } else {
                        FXUtil.handleErrors(null, res, "Error! couldn't fetch work from server");
                    }
                }
            });
        }
    }


    // pushing schedule
    private void schedulePushForFinishedTargets() {
        timerForPushing.schedule(new TimerTask() {
            @Override
            public void run() {
                pushResultsToServer();
            }
        }, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    private void pushResultsToServer() {
        if (!finishedLogs.isEmpty() && !finishedWork.isEmpty()) {

            String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/finished-work")
                    .newBuilder()
                    .addQueryParameter("username", workerName)
                    .build()
                    .toString();

            RequestBody body = getRequestBody();

            if (body == null)
                return;

            Request request = new Request.Builder()
                    .url(finalUrl)
                    .post(body)
                    .build();

            HttpUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    FXUtil.handleErrors(e, "", "Error! couldn't fetch work from server");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String res = response.body().string();
                    if (response.isSuccessful()) {
                        System.out.println(res);
                    } else {
                        FXUtil.handleErrors(null, res, "Error! couldn't send results to server");
                    }
                }
            });
        }
    }

    private RequestBody getRequestBody() {
        int newIndex = getListCurrentIndex(finishedLogs);
        if (newIndex == lastPushedIndex)
            return null;

        lastPushedIndex = newIndex;
        return RequestBody.create(
                HttpUtil.GSON.toJson(finishedLogs, new TypeToken<List<accumulatorForWritingToFile>>() {
                }.getType())
                        + "~~~" +
                        HttpUtil.GSON.toJson(finishedLogs, new TypeToken<List<TaskTarget>>() {
                        }.getType()),
                MediaType.parse("application/json"));
    }
    // ----------------------------------------- server call management ----------------------------------------------//


    // ------------------------------------------- engine management -------------------------------------------------//
    public TargetEngine(int poolSize, String workerName) {

        this.workerName = workerName;
        if (poolSize < 6 && poolSize > 0) {
            this.poolSize = poolSize;
            pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
        } else
            throw new IllegalArgumentException("Pool size must be between 1 and 5");
    }

    public void addTask(String taskName) {
        if (timerForFetching == null) {
            timerForFetching = new Timer();
            scheduleTargetFetching();
        }
        if (!tasksImIn.contains(taskName)) {
            tasksImIn.add(taskName);
            pauseFetching = false;
        }
    }

    public void removeTask(String taskName) {
        tasksImIn.remove(taskName);
        if (tasksImIn.isEmpty())
            pauseFetching = true;
    }

    public synchronized void updateNumberOfActiveThreads(int delta) {
        activeThreads += delta;
    }

    public synchronized int getFreeThreads() {
        return activeThreads;
    }

    public synchronized <T> int getListCurrentIndex(List<T> list) {

        return list.size();
    }

    // ------------------------------------------- engine management -------------------------------------------------//
    public void addTasksToQueue(List<TaskTarget> taskTargets, String s) {
        taskTargets.forEach(target -> {
            if ("COMPILATION".equals(s)) {
                performCompilation(target);
            } else {
                performSimulation(target);
            }
        });
    }

    private synchronized void saveToResults(TaskTarget target, accumulatorForWritingToFile targetLogs) {
        this.finishedWork.add(target);
        this.finishedLogs.add(targetLogs);
        updateNumberOfActiveThreads(-1);
        if (timerForPushing == null) { //lazy
            timerForPushing = new Timer();
            schedulePushForFinishedTargets();
        }
    }


    //compilation
    private void performCompilation(TaskTarget target) {
        accumulatorForWritingToFile targetLogs = new accumulatorForWritingToFile();
        Thread t = new Thread(() -> {
            try {
                targetLogs.startTime = System.currentTimeMillis();
                String fullCommand = getFullCommand(target, targetLogs);
                Process p = Runtime.getRuntime().exec(fullCommand);
                p.waitFor();
                targetLogs.endTime = System.currentTimeMillis();
                String javacErrorMessage = "";
                if (p.exitValue() == 0) { // 0 == success
                    target.state = TargetState.SUCCESS;
                } else {
                    target.state = TargetState.FAILURE;
                    javacErrorMessage = new BufferedReader(
                            new InputStreamReader(p.getErrorStream())).readLine();
                }
                compilationACUpdate(targetLogs, target, fullCommand, javacErrorMessage);
                saveToResults(target, targetLogs);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }, target.taskName + " " + target.name);
        pool.execute(t);
    }

    private void compilationACUpdate(accumulatorForWritingToFile resOfTargetTaskRun, TaskTarget targetToExecute,
                                     String fullCommand, String javacErrorMessage) {
        Timestamp ts = new Timestamp(resOfTargetTaskRun.startTime);

        resOfTargetTaskRun.targetName = targetToExecute.name;
        resOfTargetTaskRun.targetState = targetToExecute.state;
        resOfTargetTaskRun.UserData = targetToExecute.userData;

        resOfTargetTaskRun.totalTimeToRun = resOfTargetTaskRun.endTime - resOfTargetTaskRun.startTime;
        resOfTargetTaskRun.outPutData.add("* Working on target: " + targetToExecute.name);
        resOfTargetTaskRun.outPutData.add("* Task was ran by : " + workerName);
        resOfTargetTaskRun.outPutData.add("* Task started compiling: " + ts.toString().substring(10));
        resOfTargetTaskRun.outPutData.add("* File being compiled: " + targetToExecute.userData);
        resOfTargetTaskRun.outPutData.add("* The command used to compile: " + fullCommand);
        ts.setTime(resOfTargetTaskRun.endTime);
        resOfTargetTaskRun.outPutData.add("* The task finished compiling: " + ts.toString().substring(10));
        resOfTargetTaskRun.outPutData.add("* Outcome of the task: " + targetToExecute.state);
        resOfTargetTaskRun.outPutData.add("* Time taken to compile: " + TimeUtil.ltd(resOfTargetTaskRun.totalTimeToRun));
        // perform on server side after updating the task about the results
        // resOfTargetTaskRun.outPutData.add("* Targets opened : " + resOfTargetTaskRun.targetOpened);
        if (!javacErrorMessage.isEmpty()) {
            resOfTargetTaskRun.outPutData.add("* Error message from javac: " + javacErrorMessage);
            //resOfTargetTaskRun.outPutData.add("* Target skipped due to failure: " + resOfTargetTaskRun.SkippedTargets);
        }
    }

    private String getFullCommand(TaskTarget target, accumulatorForWritingToFile resOfTargetTaskRun) {
        String saveCompiledFilesTo = target.dstFolderPath;
        String FQN = target.userData.replace(".", "\\");
        String srcFileToCompile = target.srcFolderPath + "\\" + FQN + ".java";
        // spacing are important!!!!!! motherfucker
        return "javac " + "-d " + saveCompiledFilesTo + " -cp " + saveCompiledFilesTo + " " + srcFileToCompile;
    }


    // simulation
    private void performSimulation(TaskTarget target) {
        accumulatorForWritingToFile targetLogs = new accumulatorForWritingToFile();
        Thread t = new Thread(() -> {
            simulateRun(target, targetLogs);
            updateStatusAccordingToResults(target);
            saveToResults(target, targetLogs);
        });
        t.setName(target.taskName + " " + target.name);
        pool.execute(t);
    }

    private void simulateRun(TaskTarget target, accumulatorForWritingToFile targetLogs) {
        targetLogs.startTime = System.currentTimeMillis();
        Timestamp ts = new Timestamp(targetLogs.startTime);
        try {
            if (target.isRandom) {
                int randomNumberToWait = random.nextInt(target.msToRun);
                targetLogs.outPutData.add("  * going to sleep for " + TimeUtil.ltd(randomNumberToWait));

                targetLogs.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(randomNumberToWait);
            } else {
                targetLogs.outPutData.add("  * going to sleep for " + TimeUtil.ltd(target.msToRun));

                targetLogs.outPutData.add("  * going to sleep, good night " + ts.toString().substring(10));
                Thread.sleep(target.msToRun);
            }
            targetLogs.endTime = System.currentTimeMillis();
            ts.setTime(targetLogs.endTime);

            targetLogs.outPutData.add("  * top of the morning to ya good sir " + ts.toString().substring(10));
            targetLogs.totalTimeToRun = targetLogs.endTime - targetLogs.startTime;

        } catch (InterruptedException e) { /**/ }
        targetLogs.endTime = System.currentTimeMillis();
    }

    private void updateStatusAccordingToResults(TaskTarget target) {
        if (random.nextDouble() <= target.successRate) {
            target.state = TargetState.SUCCESS;
            if (random.nextDouble() <= target.successfulWithWarningRate) {
                target.state = TargetState.WARNING;
            }
        } else {
            target.state = TargetState.FAILURE;
        }
    }
}