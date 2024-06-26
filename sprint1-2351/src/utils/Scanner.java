package utils;

import java.util.Map;
import java.util.HashMap;

import annotation.ControlleurAnnotation;

public class Scanner {
    public static Map<String,Class> scanCurrentProjet(String packageName){
        Map<String,Class> res = new HashMap<>();
        try{
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            String path = packageName.replace(".", "/");
            java.net.URL ressource = classLoader.getResource(path);
            java.io.File directory = new java.io.File(ressource.getFile());

            for(java.io.File file : directory.listFiles()){
                if(file.getName().endsWith(".class")){
                    String className = packageName + "."+ file.getName().substring(0,file.getName().length() - 6);
                    Class<?> cl = Class.forName(className);
                    ControlleurAnnotation annot = cl.getAnnotation(ControlleurAnnotation.class);
                    if(annot != null){
                        res.put(annot.url(),cl);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return res;
        

    }
    public static void main(String[] args) {
        Map<String,Class> map = scanCurrentProjet("")
        }
    }
}
