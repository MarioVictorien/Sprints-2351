package sprint;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;


import utils.Scanner;
import utils.Mapping;
import utils.ModelView;
import utils.MySession;

import annotation.Param;
import annotation.RestAPI;

public class FrontController extends HttpServlet {
    Map<String, Mapping> urlMap;
    ArrayList<String> urlView;
    String message;
    boolean visited;

    public void init() throws ServletException {
        visited = false;
        String controllPackage = this.getInitParameter("controllPackage");
        try {
            this.urlMap = Scanner.scanMethod(controllPackage);
            this.urlView = Scanner.scanView("../..");
        } catch (Exception e) {
            message = "Erreur au niveau du build du projet. Veuillez consulter votre terminal";
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        processRequest(req, rep);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse rep)
            throws ServletException, IOException {
        String prevu = req.getMethod();
        PrintWriter out = rep.getWriter();
        Map<String, String> paramMap = new HashMap<>();
        String url = req.getRequestURL().toString();
            String[] urP = url.split("\\?"); // separation du lien et des parametres dans le liens
            String[] urlParts = urP[0].split("/"); // recuperation des differentes parties du lien
            int i = 1;
            String urlTarget = "/" + urlParts[urlParts.length - 1];
            boolean ver = false;
            try {
                this.methodExist(url);
                while (i < urlParts.length) {
                    if (this.urlMap.containsKey(urlTarget)) {
                        Mapping mapping = this.urlMap.get(urlTarget);
                        if(!mapping.contains(prevu)){//verifie si post ou get 
                            String verbActu = mapping.getVerbAction().get(0).getVerb();
                            out.println("Erreur de requête : Une requête de type "+prevu+" est attendue."+verbActu+" a été trouvée pour la methode");
                        }
                        try {
                            Object obj = executeMethode(mapping, req,prevu);
                            if (obj instanceof String) {
                                if(isJson((String)obj)){ //si l'objet est au format json
                                    rep.setContentType("application/json");
                                    rep.setCharacterEncoding("UTF-8");
                                }
                                out.print((String) obj);
                            } else if (obj instanceof ModelView) {
                                ModelView modelV = (ModelView) obj;
                                Map<String, Object> map = modelV.getData();
                                try {
                                    this.viewExist(modelV.getViewUrl());
                                    RequestDispatcher dispat = req.getRequestDispatcher(modelV.getViewUrl());
                                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                                        String dataName = (String) entry.getKey();
                                        Object data = entry.getValue();
                                        req.setAttribute(dataName, data);
                                    }
                                    dispat.forward(req, rep);
                                } catch (Exception e) {
                                    out.print(e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            RequestDispatcher dispat = req.getRequestDispatcher("erreur.jsp");
                            req.setAttribute("erreur",e.getMessage());
                            dispat.forward(req,rep);
                            out.println(e.getMessage());
                        }
                        ver = true;
                        break;
                    } else {
                        urlTarget = "/" + urlParts[urlParts.length - (i + 1)] + urlTarget;
                    }
                    i++;
                }
            } catch (Exception e) {
                out.println(e.getMessage());
            }
    }

    public static Object executeMethode(Mapping target,HttpServletRequest req,String prevu) throws Exception{
        //recuperation des noms des parametres
        Enumeration<String> paramNames = req.getParameterNames();
        ArrayList<String> parametersNames = new ArrayList<>();
        while(paramNames.hasMoreElements()){
            String paramName = paramNames.nextElement();
            parametersNames.add(paramName); //stockage des noms dans une liste
        } 

        String className = target.getClassName(); //nom de la classe contenu dans le mapping
        String methodeName = target.getMethodName(prevu); //nom de la methode a invoquée
        Class<?>cl = Class.forName(className);//Recuperation de la classe qui va invoquer la methode
        Method[] mes = cl.getDeclaredMethods();//Recuperation de la liste des methodes
        boolean hasAnnot = true;
        //recherche de la methode correspondante
        for(Method m : mes){
            if(target.getMethodName(prevu).compareTo(m.getName()) == 0){ //si le nom correspond
                Method me = m;
                Parameter[] parms = me.getParameters(); //recuperation des parametres de la methode
                int countParam = parms.length; //nombre d'argument de la methode
                Object instance = cl.getDeclaredConstructor().newInstance(); //instanciation de l'objet qui va executer la methode
                Object obj = null;
                boolean isRestAPI = false; //dit qu'il n'y a pas d'annotation restAPI
                if(me.getAnnotation(RestAPI.class) != null){ //si il y a l'annotation restAPI
                    isRestAPI = true;
                }
                if(countParam >= 1){ //si la methode possede des parametres
                    ArrayList<Object> paramO = new ArrayList<>();
                    ArrayList<String>passage = new ArrayList<>(); //pour verifier si on est pas deja passer par le parametre
                    for(Parameter p : parms){
                        Class<?> paramType = p.getType(); //recuperation du type du parametre
                        String typeName = paramType.getSimpleName(); //type du parametre
                        String annot;
                        System.out.println(paramType+"\n");
                        if(paramType.getSimpleName().compareTo("MySession") == 0){
                            HttpSession session = req.getSession();
                            MySession sess = (MySession)(paramType.getDeclaredConstructor(HttpSession.class).newInstance(session));
                            paramO.add(sess);
                        }
                        else{
                            if(p.getAnnotation(Param.class) != null){ //si le parametre possede une annotation
                                annot = p.getAnnotation(Param.class).name(); //on prend la valeur de l'annnotation
                            }else{
                                throw new Exception("ETU002351 Erreur: pas d'annotation");
                                // annot = p.getName(); // on prend le nom du parametre
                            } 
                            for(String par : parametersNames){
                                String[] paramParts = par.split("_");//separation du parametre pour savoir si on a besoin d'un objet
                                String argName = "";
                                if(paramParts.length > 1){ //si c'est le cas
                                    String objName = paramParts[0]; //nom de la classe object
                                    argName = paramParts[1]; //nom du parametre
                                    if(annot.compareTo(objName) == 0){ //validite du parametre
                                        if(!passage.contains(annot)){
                                            Object instanceParam = paramType.getDeclaredConstructor().newInstance(); //instanciation de l'objet
                                            paramO.add(instanceParam);//ajout de l'objet à la liste des parametre
                                            passage.add(annot);//marquer comme passer
                                        }
                                        Class<?> typ = Scanner.takeTypeField(paramType,argName); //recuperation du type de l'argument
                                        Object value = Scanner.convertParameterValue(typ, req.getParameter(par),argName);//Conversion du parametre
                                        Object inst = paramO.get(paramO.size() - 1); //prise du dernier parametre
                                        String methName = "set"+argName.substring(0,1).toUpperCase() + argName.substring(1); //nom du setteur correspondant
                                        Method set;
                                        if (value instanceof Integer) {
                                            set = paramType.getMethod(methName, int.class);    
                                        } else{
                                            set = paramType.getMethod(methName, value.getClass());   
                                        }
                                        set.invoke(inst,value);
                                    }
                                } else{
                                    argName = paramParts[0];
                                    if(argName.compareTo(annot) == 0){ // si il y a une correspondance, on stocke la valeur dans une liste
                                        Object value = Scanner.convertParameterValue(paramType, req.getParameter(argName),argName);//Conversion du parametre
                                        paramO.add(value); //ajout du parametre
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(methodeName);
                    System.out.println(paramO.size());
                    Object[] p = paramO.toArray(); //conversion de la liste de valeur en tableau d'objet
                    obj = me.invoke(instance,p); //invocation de la methode avec parametre
                }
                else{ //sinon invocation de la methode sans parametre
                    obj= me.invoke(instance);
                }
                if(obj.getClass().getSimpleName().compareTo("String") != 0 && obj.getClass().getSimpleName().compareTo("ModelView") != 0){ //Exception si ce n'est ni un String ni un modelView
                    throw new Exception("Erreur : la methode "+methodeName+" renvoie un objet de type "+obj.getClass().getSimpleName()+".\n Types attendus : ModelView, String");
                }
                if(isRestAPI){//si c'est un restAPI
                    if(obj.getClass().getSimpleName().compareTo("ModelView") == 0){
                        
                        obj = ((ModelView)obj).getData();
                    }
                    Gson gson = new Gson();
                    obj = gson.toJson(obj);
                }
                return obj;
            }
        }
        return null;
    }

    
    //si la vue exist
    public void viewExist(String viewUrl) throws Exception {
        ArrayList<String> listView = this.urlView;
        if (!listView.contains(viewUrl)) {
            throw new Exception("Erreur 404 : La page " + viewUrl + " n'existe pas!");
        }
    }
    //si la methodExist
    public void methodExist(String urlMethod) throws Exception {
        Map<String, Mapping> urlList = this.urlMap;
        String[] urlParts = urlMethod.split("/");
        String urlTarget = "/" + urlParts[urlParts.length - 1];
        int i = 1;
        while (i < urlParts.length) {
            if (this.urlMap.containsKey(urlTarget)) {
                return;
            }
            urlTarget = "/" + urlParts[urlParts.length - (i + 1)] + urlTarget;
            i++;
        }
        throw new Exception("Erreur 404 : L'url " + urlMethod + " n'est associé à aucune méthode du projet");
    }

    //si une chaine de caractere est au format json
    public static boolean isJson(String value){ 
        try{
            JsonElement jsonElement = JsonParser.parseString(value);
            return jsonElement.isJsonObject() || jsonElement.isJsonArray();
        } catch(JsonSyntaxException e){
            return false;
        }
    }
}
