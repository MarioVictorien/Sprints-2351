package utils;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import annotation.ControlleurAnnotation;
import annotation.Get;


public class Scanner {
    public static ArrayList<Class> scanCurrentProjet(String packageName){
        ArrayList<Class>res = new ArrayList<>();
        try{
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            String path = packageName.replace(".", "/");
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
            e.printStackTrace();
        }
        return res;
    }
    public static String buildMessage(String base,ArrayList message,String terminaison){
        String res = base;
        for(int i = 0; i<message.size(); i++){
            res += message.get(i) + terminaison + "\n";
        }
        return res;
    }

    public static Map<String,Mapping> scanMethod(String packageName) throws Exception{
        Map<String,Mapping> res = new HashMap<>();
        ArrayList<Class>listClass = scanCurrentProjet(packageName);
        ArrayList<String>message = new ArrayList<>();
        try{
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
                String mes = buildMessage("Erreur au niveau des urls:", message," est un url d'une autre method");
                throw new Exception(mes);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
}
