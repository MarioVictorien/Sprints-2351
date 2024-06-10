package utils;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import annotation.ControlleurAnnotation;
import annotation.Get;


public class Scanner {
    public static ArrayList<Class> scanCurrentProjet(String packageName)throws Exception{
        ArrayList<Class>res = new ArrayList<>();
        String path;
        try{
                Thread currentThread = Thread.currentThread();
                ClassLoader classLoader = currentThread.getContextClassLoader();
                path = packageName.replace(".", "/");
                path = path.replace("%20"," ");
                java.net.URL ressource = classLoader.getResource(path);
                java.io.File directory = new java.io.File(ressource.getFile());
                System.out.println(directory.listFiles().length);
                for(java.io.File file : directory.listFiles()){
                    if(file.getName().endsWith(".class")){
                        String className = packageName + "."+ file.getName().substring(0,file.getName().length() - 6);
                        Class<?> cl = Class.forName(className);
                        ControlleurAnnotation annot = cl.getAnnotation(ControlleurAnnotation.class);
                        if(annot != null){
                            res.add(cl);
                        }
                    }
                }
        }catch(Exception e){
            String message = "Le nom de package "+packageName+" n'existe pas dans le projet";
            throw new Exception(message);
        }
        return res;
    }
    public static String buildMessage(String base,ArrayList message,String terminaison){
        String res = base + "\n";
        for(int i = 0; i<message.size(); i++){
            res += message.get(i) + terminaison + "\n";
        }
        return res;
    }

    public static Map<String,Mapping> scanMethod(String packageName) throws Exception{
        Map<String,Mapping> res = new HashMap<>();
        ArrayList<String>message = new ArrayList<>();
        try{
            ArrayList<Class>listClass = scanCurrentProjet(packageName);
            for(Object c : listClass){
                Class cl = (Class)c;
                Method[] listMethod = cl.getDeclaredMethods();
                for(Method me : listMethod){
                    Get a = me.getAnnotation(Get.class);
                    if(a != null){
                        if(res.containsKey(a.url())){
                            message.add(a.url());
                        }
                        else {
                            String className = cl.getName();
                            String methodName = me.getName();
                            Mapping m = new Mapping(className,methodName);
                            res.put(a.url(),m);
                        }
                    }
                }
            }
            if(message.size() > 0){
                String mes = buildMessage("Erreur au niveau des urls:", message," est un url d'une autre methode");
                Exception ex =  new Exception(mes);
                throw ex;
            }
        } catch(Exception e){
            throw new Exception(e.getMessage());
        }
        return res;
    }
    
    public static ArrayList<String> scanView(String container) throws Exception{
        ArrayList<String>res = new ArrayList<>();
        String path;
        Thread currentThread = Thread.currentThread();
        ClassLoader classLoader = currentThread.getContextClassLoader();
        path = container.replace("%20"," ");
        java.net.URL ressource = classLoader.getResource(path);
        java.io.File directory = new java.io.File(ressource.getFile());
        res.add(directory.getAbsolutePath());
        for(java.io.File file : directory.listFiles()){
            if(file.getName().endsWith(".jsp")){
                String fileName = file.getName();
                res.add(fileName);
            }
        }
        return res; 
    }
}
