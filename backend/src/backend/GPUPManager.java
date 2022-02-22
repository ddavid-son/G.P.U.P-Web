package backend;

import argumentsDTO.CommonEnums.*;
import argumentsDTO.CommonEnums.RelationType;
import argumentsDTO.TaskArgs;
import argumentsDTO.TaskTarget;
import argumentsDTO.accumulatorForWritingToFile;
import dataTransferObjects.*;
import javafx.scene.paint.Color;
import sun.rmi.runtime.Log;

import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.RED;


public class GPUPManager {
    private final Map<String, Engine> GPUPEngines = new HashMap<>();
    private final Map<String, User> users = new HashMap<>(); // jsessionid -> user name // now with username cookie this is no longer necessary
    private final Map<String, TaskManager> tasks = new HashMap<>(); // taskName TO taskManager
    private final Map<String, User> username2User = new HashMap<>();
    private final Map<String, Integer> taskName2MaxIndex = new HashMap<>();


    // ctor and utils refreshers
    public GPUPManager() {

    }

    private void addEngine(String engineName, Engine engine) {
        GPUPEngines.put(engineName, engine);
    }

    public boolean addEngine(Engine engine) {
        if (!GPUPEngines.containsKey(engine.getGraphName())) {
            addEngine(engine.getGraphName(), engine);
            return true;
        }
        return false;
    }

    public boolean engineExists(String engineName) {
        return GPUPEngines.containsKey(engineName);
    }

    public Engine getEngineByName(String engineName) {
        if (engineExists(engineName)) {
            return GPUPEngines.get(engineName);
        }
        return null;
    }

    public boolean addTask(TaskManager taskManager) {
        if (!tasks.containsKey(taskManager.getTaskName())) {
            tasks.put(taskManager.getTaskName(), taskManager);
            taskName2MaxIndex.put(taskManager.getTaskName(), 0);
            return true;
        }
        // consider adding the task with updated name here
        return false;
    }

    public List<String> getTasks() {
        return new ArrayList<>(tasks.keySet());
    }

    public int acceptResults(List<TaskTarget> targets, List<accumulatorForWritingToFile> targetLogs) {
        int totalWorkPrice = 0;
        while (!targets.isEmpty()) {
            totalWorkPrice += sendDataToTask(targets.remove(0), targetLogs.remove(0));
        }

        return totalWorkPrice;
    }

    private int sendDataToTask(TaskTarget target, accumulatorForWritingToFile log) {
        if (taskExists(target.taskName)) {
            return tasks.get(target.taskName).finishWork(target, log);
        }
        return 0;
    }


    // worker dashboard
    public List<TaskInfoDTO> getTaskInfo(String userName) {
        List<TaskInfoDTO> taskInfoDTOS = new ArrayList<>();
        tasks.values().forEach(task -> taskInfoDTOS.add(task.getTaskDto(userName)));
        return taskInfoDTOS;
    }

    public boolean taskExists(String taskName) {
        return tasks.containsKey(taskName);
    }

    public boolean taskInCompatibleState(String taskName, String joinOrLeave) {
        if (joinOrLeave.equals("join"))
            return tasks.get(taskName).getStatus().equals(TaskStatus.CANCELED.toString()) ||
                    tasks.get(taskName).getStatus().equals(TaskStatus.FINISHED.toString());
        return true;
    }

    public void joinTask(String taskName, String workerName) {
        if (!"CANCELLED".equals(tasks.get(taskName).getStatus()) && !"FINISHED".equals(tasks.get(taskName).getStatus())) {
            tasks.get(taskName).addWorker(workerName);
            username2User.get(workerName).addTask(taskName);
        }
    }

    public void leaveTask(String taskName, String workerName) {
        tasks.get(taskName).removeWorker(workerName);
        username2User.get(workerName).removeTask(taskName);
    }

    public List<TaskTarget> getWorkForWorker(String workerName, int askedWork) {
        List<String> tasksImIn = username2User.get(workerName).tasksImIn;
        List<TaskTarget> workToSend = new ArrayList<>();
        tryToGetOneOfEach(askedWork, tasksImIn, workToSend);
        tryToGetMaxWorkAsked(askedWork, tasksImIn, workToSend);

        return workToSend;
    }

    private void tryToGetMaxWorkAsked(int askedWork, List<String> tasksImIn, List<TaskTarget> workToSend) {
        for (String taskName : tasksImIn) {
            if (workToSend.size() == askedWork)
                break;
            workToSend.addAll(
                    tasks.get(taskName).getTargetToExecute(
                            askedWork - workToSend.size()
                    )
            );
        }
    }

    private void tryToGetOneOfEach(int askedWork, List<String> tasksImIn, List<TaskTarget> workToSend) {
        for (String taskName : tasksImIn) {
            if (workToSend.size() == askedWork)
                break;
            List<TaskTarget> t = tasks.get(taskName).getTargetToExecute();
            if (!t.isEmpty())
                workToSend.add(t.get(0));
        }
    }


    // users dashboards
    public List<UserDto> getUsers() {
        List<UserDto> userDto = new ArrayList<>();
        users.values().forEach(user -> userDto.add(new UserDto(user.username, user.role)));
        return userDto;
    }

    public String getUserFromJSID(String jsessionid) {
        return users.get(jsessionid).username;
    }

    public void addUser(String jsessionid, String userName, String role) {
        User user = new User(userName, role);
        users.put(jsessionid, user);
        username2User.put(userName, user);
    }

    public boolean userExists(String username) {
        return username2User.containsKey(username);
    }

    public boolean userExists(String username, String role) {
        return userExists(username) && username2User.get(username).role.equals(role);
    }

    public boolean userInRole(String username, String role) {
        return userExists(username) && username2User.get(username).role.equals(role);
    }

    public boolean createTaskFromExistingTask(String taskName, boolean isIncremental) {

        TaskArgs taskArgs = tasks.get(taskName).getTaskArgsForNewTask(isIncremental);

        if (taskArgs == null) {
            return false;
        }

        taskName2MaxIndex.put(taskName, taskName2MaxIndex.get(taskName) + 1);
        taskArgs.setTaskName(taskArgs.getTaskName() + " " + taskName2MaxIndex.get(taskName));

        addTask(GPUPEngines.get(taskArgs.getOriginalGraph())
                .buildTask(taskArgs, null, null)
        );
        return true;
    }


    // admin dashboard
    public List<String> getLoadedGraphs() {
        return GPUPEngines.values().stream().map(Engine::getGraphName).collect(Collectors.toList());
    }

    public GraphInfoDTO getGraphPeek(String graphName) {
        return GPUPEngines.get(graphName).getGraphInfo();
    }

    public List<InfoAboutTargetDTO> getInfoAboutAllTargets(String graphName) {
        return GPUPEngines.get(graphName).getInfoAboutAllTargets();
    }

    public void setNewStatuse(String taskStatus, String taskName) {
        switch (taskStatus) {
            case "ACTIVE":
                if (tasks.get(taskName).getStatus().equals("NEW")) {
                    tasks.get(taskName).activateTask();
                } else {
                    tasks.get(taskName).resumeTask();
                }
                break;
            case "PAUSED":
                tasks.get(taskName).pauseTask();
                break;
            case "CANCELED":
                tasks.get(taskName).cancelledTask();
                break;
        }
    }

    public List<String> getInfoAboutClockedTarget(String taskName, String targetName, String targetState) {
        return tasks.get(taskName).getTargetClickedInfo(targetName, stateToColor(targetState));
    }

    private TargetState stateToColor(String state) {
        switch (state) {
            case "IN_PROGRESS":
                return TargetState.IN_PROCESS;
            case "SKIPPED":
                return TargetState.SKIPPED;
            case "FAILURE":
                return TargetState.FAILURE;
            case "FROZEN":
                return TargetState.FROZEN;
            case "WAITING":
                return TargetState.WAITING;
            case "WARNING":
                return TargetState.WARNING;
            case "SUCCESS":
                return TargetState.SUCCESS;
        }
        return TargetState.SKIPPED;
    }

    public List<String> getLogs(int lastVisitedLog, String taskName) {
        return tasks.get(taskName).getLogsDelta(lastVisitedLog).stream()
                .map(accumulatorForWritingToFile::getLogsAsString)
                .collect(Collectors.toList());
    }


    // ex2 addons
    public List<String> getCircle(String engineName, String targetName) {
        return GPUPEngines.get(engineName).findIfTargetIsInACircle(targetName);
    }

    public List<String> getAllTargets(String engineName) {
        return GPUPEngines.get(engineName).getAllTargetNames();
    }

    public List<String> getAllPaths(String engineName, String src, String dst) {
        return GPUPEngines.get(engineName)
                .findAllPathsBetweenTargets(src, dst).stream()
                .map(path -> String.join(" -> ", path))
                .collect(Collectors.toList());
    }

    public WhatIfDTO getRelated(String engineName, String targetName, String relation) {
        return GPUPEngines.get(engineName).getWhatIf(targetName, "DEPENDS_ON".equals(relation) ?
                RelationType.DEPENDS_ON :
                RelationType.REQUIRED_FOR
        );
    }

    public UpdateListsDTO getUpdateListsDTO(String taskName) {
        UpdateListsDTO t = tasks.get(taskName).getUpdateListsDTO();
        t.setTaskLogs(getLogs(0, taskName));
        return t;
    }

    public TaskArgs getTaskArgs(String taskName) {
        return tasks.get(taskName).getTaskArgs();
    }

    public List<WorkerTaskInfoDto> getWorkerTaskInfo(String userName) {
        List<WorkerTaskInfoDto> workerTaskInfoDtos = new ArrayList<>();
        for (String s : username2User.get(userName).getTasksImIn()) {
            workerTaskInfoDtos.add(tasks.get(s).getWorkerTaskinfo());
        }
        return workerTaskInfoDtos;
    }
}
