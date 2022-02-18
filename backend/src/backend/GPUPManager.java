package backend;

import argumentsDTO.CommonEnums.*;
import argumentsDTO.CommonEnums.RelationType;
import argumentsDTO.TaskArgs;
import argumentsDTO.TaskTarget;
import argumentsDTO.accumulatorForWritingToFile;
import dataTransferObjects.*;

import java.util.*;
import java.util.stream.Collectors;


public class GPUPManager {
    private Map<String, Engine> GPUPEngines = new HashMap<>();
    private Map<String, User> users = new HashMap<>(); // jsessionid -> user name // now with username cookie this is no longer necessary
    private Map<String, TaskManager> tasks = new HashMap<>(); // taskName TO taskManager
    private Map<String, User> username2User = new HashMap<>();

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
        //

        if (!tasks.containsKey(taskManager.getTaskName())) {
            tasks.put(taskManager.getTaskName(), taskManager);
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
    public TaskInfoDTO getTaskInfo(String taskName, String userName) {
        if (tasks.containsKey(taskName)) {
            return tasks.get(taskName).getTaskDto(userName);
        }
        return null;
    }

    public boolean taskExists(String taskName) {
        return tasks.containsKey(taskName);
    }

    public boolean taskInCompatibleState(String taskName, String joinOrLeave) {
        if (joinOrLeave.equals("join"))
            return tasks.get(taskName).getStatus().equals(TaskStatus.CANCELED) ||
                    tasks.get(taskName).getStatus().equals(TaskStatus.FINISHED);
        return true;
    }

    public void joinTask(String taskName, String workerName) {
        tasks.get(taskName).addWorker(workerName);
        users.get(workerName).addTask(taskName);
    }

    public void leaveTask(String taskName, String workerName) {
        tasks.get(taskName).removeWorker(workerName);
        users.get(workerName).removeTask(taskName);
    }

    public List<TaskTarget> getWorkForWorker(String workerName, int askedWork) {
        List<String> tasksImIn = users.get(workerName).tasksImIn;
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
            TaskTarget t = tasks.get(taskName).getTargetToExecute();
            if (t != null)
                workToSend.add(t);
        }
    }


    // users dashboards
    public List<UserDto> getUsers() {
        List<UserDto> userDto = new ArrayList<>();
        users.values().forEach(user -> {
            userDto.add(new UserDto(user.username, user.role));
        });
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
        return tasks.get(taskName).getUpdateListsDTO();
    }

    public TaskArgs getTaskArgs(String taskName) {
        return tasks.get(taskName).getTaskArgs();
    }
}
