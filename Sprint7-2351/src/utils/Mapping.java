package utils;

public class Mapping {
    String className;
    String methodName;
    public Mapping(String cl,String method){
        this.setClassName(cl);
        this.setMethodName(method);
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
}