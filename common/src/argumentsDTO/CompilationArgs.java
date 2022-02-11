package argumentsDTO;

import argumentsDTO.CommonEnums.*;

public class CompilationArgs extends TaskArgs {

    private String srcPath;
    private String dstPath;

    public CompilationArgs(boolean isWhatIf, int numOfThreads, boolean isIncremental,
                           RelationType relationType, String srcPath, String dstPath) {
        super(isWhatIf, numOfThreads, CommonEnums.TaskType.COMPILATION, isIncremental, relationType);
        this.dstPath = dstPath;
        this.srcPath = srcPath;
    }
    
    public String getSrcPath() {
        return srcPath;
    }

    public String getDstPath() {
        return dstPath;
    }
}
