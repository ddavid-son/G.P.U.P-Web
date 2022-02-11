package backend;

import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import dataTransferObjects.UserDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GPUPManager {
    private Map<String, Engine> GPUPEngines = new HashMap<String, Engine>();
    private Map<String, User> users = new HashMap<>(); // jsessionid -> user name // now with username cookie this is no longer necessary

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

    //

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
        users.put(jsessionid, new User(userName, role));
    }

    //

    public List<String> getLoadedGraphs() {
        return GPUPEngines.values().stream().map(Engine::getGraphName).collect(Collectors.toList());
    }

    public GraphInfoDTO getGraphPeek(String graphName) {
        return GPUPEngines.get(graphName).getGraphInfo();
    }

    public List<InfoAboutTargetDTO> getInfoAboutAllTargets(String graphName) {
        return GPUPEngines.get(graphName).getInfoAboutAllTargets();
    }

    //

    public List<String> getCircle(String engineName, String targetName) {
        return GPUPEngines.get(engineName).findIfTargetIsInACircle(targetName);
    }

    public List<String> getAllTargets(String engineName) {
        return GPUPEngines.get(engineName).getAllTargetNames();
    }
}
