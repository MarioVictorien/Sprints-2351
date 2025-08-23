package utils;

import java.util.ArrayList;

public class Mapping {
    String className;
    ArrayList<VerbAction> verbAction;
    
    public Mapping(String cl){
        this.setClassName(cl);
    }
    public Mapping(){}

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public String getMethodName(String verb){
        for(VerbAction va : this.verbAction){
            if(va.getVerb().equals(verb)){
                return va.getMethodName();
            }
        }
        return null;
    }
    public ArrayList<VerbAction> getVerbAction() {
        return verbAction;
    }
    public void setVerbAction(ArrayList<VerbAction> verbAction) {
        this.verbAction = verbAction;
    }
    public void addVerbAction(String methode,String verb){
        if(this.verbAction == null){
            this.verbAction = new ArrayList<>();
        }
        this.verbAction.add(new VerbAction(methode,verb));
    }
    public boolean contains(String verb){
        for(VerbAction ver : this.verbAction){
            if(ver.getVerb().equalsIgnoreCase(verb)){
                return true;
            }
        }
        return false;
    }
    
}