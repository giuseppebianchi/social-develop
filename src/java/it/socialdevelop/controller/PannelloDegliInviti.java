/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.socialdevelop.controller;

import it.univaq.f4i.iw.framework.data.DataLayerException;
import it.univaq.f4i.iw.framework.result.FailureResult;
import it.univaq.f4i.iw.framework.result.StreamResult;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import it.socialdevelop.data.model.Admin;
import it.socialdevelop.data.model.CollaborationRequest;
import it.socialdevelop.data.model.Developer;
import it.socialdevelop.data.model.Files;
import it.socialdevelop.data.model.Project;
import it.socialdevelop.data.model.SocialDevelopDataLayer;
import it.socialdevelop.data.model.Task;

/**
 *
 * @author Hello World Group
 */
public class PannelloDegliInviti extends SocialDevelopBaseController {
    
    private void action_error(HttpServletRequest request, HttpServletResponse response) {
        if (request.getAttribute("exception") != null) {
            (new FailureResult(getServletContext())).activate((Exception) request.getAttribute("exception"), request, response);
        }
    }
    
    
    
    private void getImg(HttpServletRequest request, HttpServletResponse response, Developer dev) throws IOException, SQLException, DataLayerException, NamingException {
        StreamResult result = new StreamResult(getServletContext());
        
         SocialDevelopDataLayer datalayer = (SocialDevelopDataLayer) request.getAttribute("datalayer");
         if(dev.getFoto() != 0){
            Files foto_profilo = datalayer.getFile(dev.getFoto());
            request.setAttribute("foto_profilo", "uploaded-images/" + foto_profilo.getLocalFile());
         }else{
            request.setAttribute("foto_profilo", "uploaded-images/foto_profilo_default.png");             
         }
        
    }
    
    
    
    private void action_inviti(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, TemplateManagerException, SQLException, NamingException, DataLayerException {
            HttpSession s = request.getSession(true);
            request.setAttribute("page_title", "Panel of Invites");
            request.setAttribute("page_subtitle", "manage your invites");
            if (s.getAttribute("userid") != null && ((int) s.getAttribute("userid"))>0) {
                SocialDevelopDataLayer datalayer = (SocialDevelopDataLayer) request.getAttribute("datalayer");
                Admin admin = datalayer.getAdmin((int) s.getAttribute("userid"));
                if(admin!=null){
                    request.setAttribute("admin", "admin");
                }
                //recuperiamo sviluppatore a cui appartiene il pannello
                Developer dev = datalayer.getDeveloper((int) s.getAttribute("userid"));
                request.setAttribute("username", dev.getUsername());
                request.setAttribute("fullname", dev.getName()+" "+dev.getSurname());
                long currentTime = System.currentTimeMillis();
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(currentTime);
                 //Get difference between years
                request.setAttribute("age", now.get(Calendar.YEAR) - dev.getBirthDate().get(Calendar.YEAR));
                request.setAttribute("bio", dev.getBiography());
                request.setAttribute("mail", dev.getMail());
                request.setAttribute("logout", "Logout");
                getImg(request, response, dev);
               
                //recuperiamo gli inviti 
                List<CollaborationRequest> invites = datalayer.getInvitesByCoordinator(dev.getKey());
                List<CollaborationRequest>  invitesToSend = new ArrayList();
                for(CollaborationRequest i : invites){
                    Task task = datalayer.getTask(i.getTaskKey());
                    Project project = datalayer.getProjectByTask(task.getKey());
                    Developer invitato = datalayer.getDeveloper(i.getCollaboratorKey());
                    int foto_key = invitato.getFoto();
                    if(foto_key>0){    
                        invitato.setFotoFile(datalayer.getFile(foto_key));
                    }
                    task.setProject(project);
                    i.setTaskRequest(task);
                    i.setCollaboratorRequest(invitato);
                    invitesToSend.add(i);
                }
                
                request.setAttribute("invites", invitesToSend);
                TemplateResult res = new TemplateResult(getServletContext());
                res.activate("pannello_degli_inviti.html",request, response);
                
            }else{
                 response.sendRedirect("index");
            }
            
           
    }
    
    
    
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException{
        
        try {
            action_inviti(request, response);
        } catch (IOException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        } catch (TemplateManagerException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        } catch (SQLException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        } catch (NamingException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);
        } catch (DataLayerException ex) {
            request.setAttribute("exception", ex);
            action_error(request, response);        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}