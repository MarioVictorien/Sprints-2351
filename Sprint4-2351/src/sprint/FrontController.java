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
    String message;
    boolean visited;
    public void init() throws ServletException{
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        try{
            this.urlMap = Scanner.scanMethod(controllPackage);
        } catch(Exception e){
            message = e.getMessage();
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
            if(message != null){
                out.println(message);
            }else{
                while(i < urlParts.length){
                    if(this.urlMap.containsKey(urlTarget)){
                        Mapping mapping = this.urlMap.get(urlTarget);
                        out.println(String.format("La methode %s de la classe %s a ete appele\n", mapping.getMethodName(),mapping.getClassName()));
                        try{
                            executeMethode(mapping,out,req,rep);
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
                if(!ver){
                    out.print(String.format("Aucune methode n'est associe à cet url : %s\n", urlTarget));
                }
                
            }
            
        }
        
    }
    public static void executeMethode(Mapping target,PrintWriter out,HttpServletRequest req, HttpServletResponse res) throws Exception{
        String className = target.getClassName();
        String methodeName = target.getMethodName();
        Class<?>cl = Class.forName(className);
        Method me = cl.getDeclaredMethod(methodeName);
        Object instance = cl.getDeclaredConstructor().newInstance();
        Object obj = me.invoke(instance);
        if(obj instanceof String){
            out.print((String)obj);
        }
        else if(obj instanceof ModelView){
            ModelView modelV = (ModelView)obj;
            Map<String,Object>map = modelV.getData();
            RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
            for(Map.Entry<String,Object> entry : map.entrySet()){
                String dataName = (String)entry.getKey();
                Object data = entry.getValue();
                req.setAttribute(dataName,data);
            }
            dispat.forward(req,res);
        } 
        else{
            String resultType = obj.getClass().getSimpleName();
            throw new Exception("Execution impossible pour le type de retour : "+resultType);
        }
    }
}

