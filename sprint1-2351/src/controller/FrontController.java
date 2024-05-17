package controller;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashMap;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

public class FrontController extends HttpServlet{
    Map<String,Class> urlMap;
    boolean verif; 
    public void init() throws ServletException{
        verif = false;
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException,IOException{
        processRequest(req, rep);
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException,IOException{
        processRequest(req, rep);
    }
    protected void processRequest(HttpServletRequest req,HttpServletResponse rep) throws ServletException,IOException{
        PrintWriter out = rep.getWriter();
        if(!verif)
        {
            String controllerpkg = this.getInitParameter("ctrlPackage");
            this.urlMap = Scanner.scanCurrentProjet(ctrlPackage);
            verif = true;
        }
        String url = req.getRequestURL().toString();
        out.print("Vous avez entrez cet url :"+url+"\n");
        out.print("Liste des controlleurs du projet : \n");
        for(String key : this.urlMap.keySet()){
            out.print("Cet url : "+ key +" est associé à la class "+ this.urlMap.get(key));
        }

    }
}