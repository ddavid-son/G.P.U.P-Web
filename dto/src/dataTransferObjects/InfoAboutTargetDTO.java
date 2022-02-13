package dataTransferObjects;

import argumentsDTO.CommonEnums.*;
import javafx.scene.control.CheckBox;

import java.util.List;

public class InfoAboutTargetDTO {
    public String userData;
    public String targetName;
    public List<String> dependsOnNames;
    public int dependsOnCount;
    public TargetType targetType;
    public boolean isSelected = false;
    public List<String> serialSetsNames;
    //public CheckBox CheckBox;

    public CheckBox getCheckBox() {
        //return CheckBox;
        return new CheckBox();
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public InfoAboutTargetDTO() {
    }

    public String getTargetTypeString() {
        return this.targetType.toString();
    }


    public List<String> requiredForNames;
    public int requiredForCount;

    public List<String> directRequiredByName;
    public int directRequiredByCount;

    public List<String> directDependsOnByName;
    public int directDependsOnByCount;

    public boolean getIsSelected() {
        return isSelected;
    }

    public String getUserData() {
        return userData;
    }

    public String getTargetName() {
        return targetName;
    }

    public List<String> getDependsOnNames() {
        return dependsOnNames;
    }

    public int getDependsOnCount() {
        return dependsOnCount;
    }

    public TargetType getTargetType() {
        return targetType;
        //return Target.TargetType.LEAF;
    }

    public List<String> getRequiredForNames() {
        return requiredForNames;
    }

    public int getRequiredForCount() {
        return requiredForCount;
    }

    public List<String> getDirectRequiredByName() {
        return directRequiredByName;
    }

    public int getDirectRequiredByCount() {
        return directRequiredByCount;
    }

    public List<String> getDirectDependsOnByName() {
        return directDependsOnByName;
    }

    public int getDirectDependsOnByCount() {
        return directDependsOnByCount;
    }

    public List<String> getSerialSetsNames() {
        return serialSetsNames;
    }

    public String getSerialSetsNamesAsString() {
        return String.join(", ", serialSetsNames);
    }

    public InfoAboutTargetDTO(String targetName, TargetType targetType, String userData,
                              List<String> allDependsOnNames, List<String> allRequiredForNames,
                              List<String> directDependsOnNames, List<String> directRequiredForNames,
                              List<String> ssNames) {
        this.targetName = targetName;
        this.targetType = targetType;
        this.userData = userData;

        this.dependsOnNames = allDependsOnNames;
        this.dependsOnCount = dependsOnNames.size();

        this.requiredForNames = allRequiredForNames;
        this.requiredForCount = requiredForNames.size();

        this.directRequiredByName = directRequiredForNames;
        this.directRequiredByCount = directRequiredByName.size();

        this.directDependsOnByName = directDependsOnNames;
        this.directDependsOnByCount = directDependsOnByName.size();

        this.serialSetsNames = ssNames;
    }

    @Override
    public String toString() {
        return "Target name = " + targetName + '\n' +
                " Target type = " + targetType + '\n' +
                " User data = " + (userData.isEmpty() ? "no user data" : userData) + '\n' +
                " Depends on = " + (dependsOnNames.isEmpty() ? "depends on no one  " : dependsOnNames) + '\n' +
                " Required for = " + (requiredForNames.isEmpty() ? "required for no one " : requiredForNames);
    }

}
