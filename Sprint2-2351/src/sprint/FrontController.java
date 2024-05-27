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
import jakarta.servlet.ServletException;

import utils.Scanner;
import utils.Mapping;

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
        String urlTarget = "/"+urlParts[urlParts.length - 1];
        if(!visited){
            out.print("Bienvenue sur la page, veuillez saisir un url!");
            visited = true;
        }
        else{
            out.print("Vous avez entrez cet url :"+url+"\n");
            if(message != null){
                out.print(message);
            }else{
                if(this.urlMap.containsKey(urlTarget)){
                    Mapping mapping = this.urlMap.get(urlTarget);
                    out.print(String.format("La methode %s de la classe %s a ete appele\n", mapping.getMethodName(),mapping.getClassName()));
                }
                else{
                    out.print(String.format("Aucune methode n'est associe à cet url : %s\n", urlTarget));
                }
            }
            
        }
        
    }
}