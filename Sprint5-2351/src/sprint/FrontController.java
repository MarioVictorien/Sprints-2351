package sprint;

import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.http.HttpServlet;    
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

import utils.Scanner;
import utils.Mapping;
import utils.ModelView;

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
        if(message != null){
            out.println(message);
        }
        else{
            String url = req.getRequestURL().toString();
            String[] urlParts = url.split("/");
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
                                Object obj = executeMethode(mapping);
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
    public static Object executeMethode(Mapping target) throws Exception{
        String className = target.getClassName();
        String methodeName = target.getMethodName();
        Class<?>cl = Class.forName(className);
        Method me = cl.getDeclaredMethod(methodeName);
        Object instance = cl.getDeclaredConstructor().newInstance();
        Object obj = me.invoke(instance);
        if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelView") != 0){
            throw new Exception("Erreur : la methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelView, String");
        }
        return obj;
        
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

