package sprint;

import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import jakarta.servlet.http.HttpServlet;    
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

import utils.Scanner;
import utils.Mapping;
import utils.ModelView;

import annotation.Param;

public class FrontController extends HttpServlet{
    Map<String,Mapping> urlMap; 
    ArrayList<String> urlView;
    String message;
    boolean visited;
    public void init() throws ServletException{
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        try{
            this.urlMap = Scanner.scanMethod(controllPackage);
            this.urlView = Scanner.scanView("../..");
        } catch(Exception e){
            message = "Erreur au niveau du build du projet. Veuillez consulter votre terminal";
            e.printStackTrace();
        }

    }
    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException,IOException{
        processRequest(req, rep);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException,IOException{
        processRequest(req, rep);
    }
    protected void processRequest(HttpServletRequest req,HttpServletResponse rep) throws ServletException,IOException{
        PrintWriter out = rep.getWriter();
        Map<String,String> paramMap = new HashMap<>();
        if(message != null){
            out.println(message);
        }
        else{
            String url = req.getRequestURL().toString();
            String[] urP = url.split("\\?"); //separation du lien et des parametres dans le liens
            String[] urlParts = urP[0].split("/"); //recuperation des differentes parties du lien
            int i = 1;
            String urlTarget = "/"+urlParts[urlParts.length - 1];
            boolean ver = false;
            if(!visited){
                out.println("Bienvenue sur la page, veuillez saisir un url!");
                visited = true;
            }
            else{
                out.print("Vous avez entrez cet url :"+url+"\n");
                try{
                    this.methodExist(url);
                    while(i < urlParts.length){
                        if(this.urlMap.containsKey(urlTarget)){
                            Mapping mapping = this.urlMap.get(urlTarget);
                            try{
                                Object obj = executeMethode(mapping,req);
                                if(obj instanceof String){
                                    out.print((String)obj);
                                }
                                else if(obj instanceof ModelView){
                                    ModelView modelV = (ModelView)obj;
                                    Map<String,Object>map = modelV.getData();
                                    try{
                                        this.viewExist(modelV.getViewUrl());
                                        RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
                                        for(Map.Entry<String,Object> entry : map.entrySet()){
                                            String dataName = (String)entry.getKey();
                                            Object data = entry.getValue();
                                            req.setAttribute(dataName,data);
                                        }
                                        dispat.forward(req,rep);
                                    } catch(Exception e){
                                        out.print(e.getMessage());
                                    }
                                }
                            } catch(Exception e){
                                out.println(e.getMessage());
                            }
                            ver = true;
                            break;
                        }
                        else{
                            urlTarget = "/"+urlParts[urlParts.length - (i + 1)]+urlTarget;
                        }
                        i++;
                    }   
                }catch(Exception e){
                    out.print(e.getMessage());
                }
            }

        }
        
        
    }
    public static Object executeMethode(Mapping target,HttpServletRequest req) throws Exception{
        //recuperation des noms des parametres
        Enumeration<String> paramNames = req.getParameterNames();
        ArrayList<String> parametersNames = new ArrayList<>();
        while(paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            parametersNames.add(paramName); //stockage des noms dans une liste
        } 

        String className = target.getClassName(); //nom de la classe contenu dans le mapping
        String methodeName = target.getMethodName(); //nom de la methode a invoquée
        Class<?>cl = Class.forName(className);
        Method[] mes = cl.getDeclaredMethods();

        //recherche de la methode correspondante
        for(Method m : mes){
            if(target.getMethodName().compareTo(m.getName()) == 0){ //si le nom correspond
                Method me = m;
                Parameter[] parms = me.getParameters(); //recuperation des parametres de la methode
                int countParam = parms.length;
                Object instance = cl.getDeclaredConstructor().newInstance();
                Object obj;
                if(countParam>0){ //si la methode possede des parametres
                    ArrayList<Object> paramO = new ArrayList<>();
                    for(Parameter p : parms){
                        if(p.getAnnotation(Param.class) != null){ //si le parametre possede une annotation
                            String annot = p.getAnnotation(Param.class).name();
                            for(String par : parametersNames){
                                if(par.compareTo(annot) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                    String value = req.getParameter(par);
                                    paramO.add(value);
                                    break;
                                }
                            }
                        }
                        else{ //si le parametre ne possede pas d'annotation
                            for(String par : parametersNames){
                                if(par.compareTo(p.getName()) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                    String value = req.getParameter(par);
                                    paramO.add(value);
                                    break;
                                }
                            }
                        }
                    }
                    Object[] p = paramO.toArray(); //conversion de la liste de valeur en tableau d'objet
                    obj = me.invoke(instance,p); //invocation de la methode avec parametre
                }
                else{ //sinon invocation de la methode sans parametre
                    obj= me.invoke(instance);
                }
                if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelView") != 0){
                    throw new Exception("Erreur : la methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelView, String");
                }
                return obj;
            }
        }
        return null;
    }
    public void viewExist(String viewUrl) throws Exception{
        ArrayList<String> listView = this.urlView;
        if(!listView.contains(viewUrl)){
            throw new Exception("Erreur 404 : La page "+viewUrl+" n'existe pas!");
        }
    }
    public void methodExist(String urlMethod) throws Exception{ 
        Map<String,Mapping> urlList = this.urlMap;
        String[] urlParts = urlMethod.split("/");
        String urlTarget = "/"+urlParts[urlParts.length - 1];
        int i = 1;
        while(i < urlParts.length){
            if(this.urlMap.containsKey(urlTarget)){
                return;
            }
            urlTarget = "/"+urlParts[urlParts.length - (i + 1)]+urlTarget;
            i++;
        }
        throw new Exception("Erreur 404 : L'url "+urlMethod+" n'est associé à aucune méthode du projet");
    }

}

