package com.kolhozcustoms.jira.plugin.wikijs.webwork;

import lombok.Data; // аннотации для авто-создания геттеров и сеттеров
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.kolhozcustoms.jira.plugin.wikijs.service.PluginSettingsService;

//import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Data
public class WikiJSConfigAction extends JiraWebActionSupport
{
    //private static final Logger log = LoggerFactory.getLogger(WikiJSConfigAction.class);
    private Boolean currentUserIsAdmin;
    private String auth_key;
    private String wiki_server;
    private String api_path;
    private PluginSettingsService pluginSettingsService;


    @Inject
    public WikiJSConfigAction(@ComponentImport UserManager userManager, PluginSettingsService pluginSettingsService){
        if (getLoggedInUser() != null && userManager.isAdmin(getLoggedInUser().getUsername())) {
            this.currentUserIsAdmin = true;
        }
        this.pluginSettingsService = pluginSettingsService;
        this.wiki_server = this.pluginSettingsService.getParameter("wiki_server", "");
        this.api_path = this.pluginSettingsService.getParameter("api_path", "/graphql");
        this.auth_key = this.pluginSettingsService.getParameter("auth_key", "");
    }

    @Override
    public String execute() throws Exception {
        if (this.currentUserIsAdmin) {
            return super.execute(); //returns SUCCESS
        }
        return ERROR;
    }
    
    public String doSave(){
        this.pluginSettingsService.setParameter("wiki_server", this.wiki_server);
        this.pluginSettingsService.setParameter("api_path", this.api_path);
        this.pluginSettingsService.setParameter("auth_key", this.auth_key);
        return SUCCESS;
    }
}

