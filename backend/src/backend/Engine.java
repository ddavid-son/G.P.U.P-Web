package backend;

import argumentsDTO.CommonEnums.*;
import argumentsDTO.ProgressDto;
import argumentsDTO.TaskArgs;
import argumentsDTO.accumulatorForWritingToFile;
import argumentsDTO.CommonEnums.TargetState;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import dataTransferObjects.WhatIfDTO;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;


public interface Engine extends Serializable {
    void pauseTask();

    void resumeTask();

    //int getMaxThreadCount();

    boolean isGraphAccessible();

    String getGraphName();

    String getOwnerName();

    boolean incrementalAvailable();

    List<String> getSerialSetList();

    List<String> getAllTargetNames();

    //void writeObjectToFile(String path);

    void setNumberOfThreads(Integer value);

    GraphInfoDTO getGraphInfo();

    //void readObjectFromFile(String filePath);

    void xmlFileLoadingHandler(InputStream inputStream);

    //void loadXmlFile(String xmlFilePath);

    List<InfoAboutTargetDTO> getInfoAboutAllTargets();

    List<String> getSerialSetTarget(String serialSetName);

    List<String> findIfTargetIsInACircle(String targetName);

    InfoAboutTargetDTO getInfoAboutTarget(String targetName);

    void makeGraphUsingGraphViz(String outPutPath, String filesNames);

    Set<List<String>> findAllPathsBetweenTargets(String start, String end);

    WhatIfDTO getWhatIf(String targetName, RelationType type);

    List<String> getInfoAboutTargetInExecution(String targetName, TargetState targetState);

    List<String> getWhatIf(List<String> targetNames, RelationType relationType);

    TaskManager buildTask(TaskArgs taskArgs, Consumer<accumulatorForWritingToFile> finishedTargetLog,
                          Consumer<ProgressDto> finishedTarget);
}
