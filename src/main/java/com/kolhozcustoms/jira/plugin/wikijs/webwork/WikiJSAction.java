package com.kolhozcustoms.jira.plugin.wikijs.webwork;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap; // sorted Map
import java.util.ArrayList;
import java.util.Arrays;
import java.text.ParseException;
import java.io.IOException;
import lombok.Data; // аннотации для авто-создания геттеров и сеттеров
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
//import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.bc.user.search.UserSearchService; //?????????
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.Avatar;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.webresource.WebResourceManager;

import com.kolhozcustoms.jira.plugin.wikijs.service.PluginSettingsService;
import webwork.action.ActionContext;
import com.google.gson.*;

@Slf4j
@Data
public class WikiJSAction extends JiraWebActionSupport
{
    //private static final Logger log = LoggerFactory.getLogger(WikiJSAction.class);
    private final UserSearchService userSearchService;
    private final AvatarService avatarService;
    private final WebResourceManager webResourceManager;
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private PluginSettingsService pluginSettingsService;
    private ApplicationUser аpplicationLoggedInUser;

    private String exception = "";
    private List<String> currentUser = new ArrayList<String>();
    private String auth_key = "";
    private String wiki_server = "";
    private String api_path = "";
    private String page = "";
    private Integer wikiUserId = 0;
    

    @Inject
    public WikiJSAction(@ComponentImport UserSearchService userSearchService, @ComponentImport AvatarService avatarService, PluginSettingsService pluginSettingsService, @ComponentImport WebResourceManager webResourceManager, @ComponentImport JiraAuthenticationContext jiraAuthenticationContext) { 
        this.userSearchService = userSearchService;

        this.avatarService = avatarService;
        this.аpplicationLoggedInUser = getLoggedInUser();
        this.currentUser.addAll(Arrays.asList(new String[] {this.аpplicationLoggedInUser.getUsername(), this.avatarService.getAvatarURL(this.аpplicationLoggedInUser, this.аpplicationLoggedInUser).toString(), this.аpplicationLoggedInUser.getDisplayName(), this.аpplicationLoggedInUser.getEmailAddress()}));
        this.webResourceManager = webResourceManager; // for include css and js in html
        
        this.pluginSettingsService = pluginSettingsService;
        this.wiki_server = this.pluginSettingsService.getParameter("wiki_server", "");
        this.api_path = this.pluginSettingsService.getParameter("api_path", "/graphql");
        this.auth_key = this.pluginSettingsService.getParameter("auth_key", "");  
        this.page = this.page == "" ? "1" : this.page;
    }

    @Override
    public String execute() throws Exception {
        if (this.wiki_server.equals("") || this.auth_key.equals("")) {
            return ERROR;
        }

        this.wikiUserId = checkUser(this.currentUser.get(3));
        if (this.wikiUserId == 0){
            createNewUser();
        }

        return super.execute(); //returns SUCCESS
    }

    private String sendPost(String json_request) throws Exception {
        HttpPost post = new HttpPost(this.wiki_server + "/graphql");
        StringEntity payload = new StringEntity(json_request, ContentType.APPLICATION_FORM_URLENCODED);

        post.addHeader(HttpHeaders.USER_AGENT, "Javazilla");
        post.addHeader("Authorization", "Bearer " +  this.auth_key);
        post.addHeader("Content-Type", "application/json");
        post.setEntity(payload);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(post);
        //HttpResponse response = httpClient.execute(post);
        
        if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
        } else {
            this.exception = this.exception + "http status code: " + response.getStatusLine().getStatusCode() + "; ";
            return null;
        }
    }

    private Integer checkUser(String userEmail) throws Exception {
        String json_request = "{\"query\":\"{ users { search (query : \\\"" + userEmail + "\\\") { id name email } }}\",\"variables\":{}}"; //e.akuzin@zephyrlab.ru
        this.exception = this.exception + json_request + "; ";
        String checkUserString = sendPost(json_request);
        return checkUserParseJSON(checkUserString);
    }

    private Integer checkUserParseJSON(String json_string){
        JsonObject responseObject = new JsonParser().parse(json_string).getAsJsonObject();
        JsonArray usersArray = responseObject.getAsJsonObject("data").getAsJsonObject("users").getAsJsonArray("search");
        if(usersArray.size() > 0) {
            this.exception = this.exception + "found user: " + usersArray.get(0).getAsJsonObject().get("name").getAsString() + "; ";
            return usersArray.get(0).getAsJsonObject().get("id").getAsInt();
        } else {
            this.exception = this.exception + "create user needed; ";
            return 0;
        }
    }

    private Void createNewUser(){
        String mutationResponse = "";
        String mutationJsonRequest = "{\"query\":\"mutation { users { create (email: \\\"" + this.currentUser.get(3) + "\\\", name: \\\"" + this.currentUser.get(2) + "\\\", passwordRaw: \\\"sdfngdfhgdxwetj\\\", groups: 2, providerKey: \\\"local\\\" ) { user { id name email } responseResult {succeeded errorCode message} } } }\",\"variables\":{}}";
        try {
            mutationResponse = sendPost(mutationJsonRequest);
        } catch (Exception e) {
            this.exception = this.exception + " mutationResponse exception: " +  e.toString() + "; ";
        }

        this.exception = this.exception + mutationResponse + "; ";
        return null;
    }
}