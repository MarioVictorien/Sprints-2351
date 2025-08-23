package utils;

public class VerbAction {
    String methodName;
    String verb;
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getVerb() {
        return verb;
    }
    public void setVerb(String verb) {
        this.verb = verb;
    }

    public VerbAction(){}
    public VerbAction(String methode, String verb){
        this.setMethodName(methode);
        this.setVerb(verb);
    }
}
