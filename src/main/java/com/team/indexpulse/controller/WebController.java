package com.team.indexpulse.controller;

import com.team.indexpulse.entity.IndexRequest;
import jakarta.persistence.Index;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import com.team.indexpulse.entity.UserAccount;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@org.springframework.stereotype.Controller
public class WebController {

    private final RestClient restClient = RestClient.create();

    @PostMapping("/user_accounts/register")
    public String postUserAccountRegister(UserAccount userAccount, Model model) {
        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<UserAccount> response = restClient
                .post()
                .uri("http://localhost:7634/user_accounts/register")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(UserAccount.class);

        if (response.getStatusCode().isError()) {//If the POST request sending fails:
            model.addAttribute("info", "Error registering user account");//WebController sends "Error posting..." message to template via info variable.
        } else if (response.getBody() == null) {//If the account returned by the API equals null:
            model.addAttribute("info", "Error registering user account");//WebController sends "Error posting..." message to template via info variable.
        } else {
            model.addAttribute("info", "User account registered");//WebController sends "User account..." message to template via info variable.
        }

        return "test";
    }

    @PostMapping("/user_accounts/login")
    public String postUserAccountLogin(UserAccount userAccount, Model model, HttpServletRequest request) {
        String id = "";//ID of user account in case of successful login. It values "" otherwise.

        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<String> response = restClient
                .post()
                .uri("http://localhost:7634/user_accounts/login")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(String.class);

        id = response.getBody();//login values the Boolean returned to the previous request.

        request.getSession().setAttribute("id", id);//We pass to all templates a session variable called id, to know the user id.

        //We pass to "test" template a request variable called info, to show data about the login status:
        if (id == null) {
            model.addAttribute("info", "Login error");
        } else if (id.isEmpty()) {//If login was not possible:
            model.addAttribute("info", "Login error");
        } else {//If login was successful:
            model.addAttribute("info", "Logged in successfully");
        }

        return "test";
    }

    @GetMapping("/index")
    public String getIndex(HttpServletRequest request) {
        return "index";
    }

    @GetMapping("/user_accounts/delete")
    public String getUserAccountDelete(Model model, HttpServletRequest request) {
        String id = request.getSession().getAttribute("id").toString();//ID of the user account to be deleted. We get it from session variables.
        if (id == null) {
            model.addAttribute("info", "Error deleting user account");//WebController sends "Error deleting..." message to template via info variable.
        } else if (id.isEmpty()) {
            model.addAttribute("info", "Error deleting user account");//WebController sends "Error deleting..." message to template via info variable.
        } else {
            //The DELETE request is sent to IndexPulseAPI.
            ResponseEntity<Void> response = restClient.delete()
                    .uri("http://localhost:7634/user_accounts/{id}", id)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode().isError()) {
                model.addAttribute("info", "Error deleting user account");//WebController sends "Error deleting..." message to template via info variable.
            } else {
                model.addAttribute("info", "Account deleted");//WebController sends "Error deleting..." message to template via info variable.
                request.getSession().setAttribute("id", null);//As we've deleted the account, the user is logged out.
            }
        }

        return "test";
    }

    @GetMapping("/user_accounts/edit")
    public String getUserAccountEdit(Model model, HttpServletRequest request) {
        String id = request.getSession().getAttribute("id").toString();//ID of the user account to be edited. We get it from session variables.
        UserAccount originalUserAccount = null;//Object with all original values of the user account.

        //The GET request is sent to IndexPulseAPI.
        UserAccount response = restClient.get()
                .uri("http://localhost:7634/user_accounts/{id}", id)
                .retrieve()
                .body(UserAccount.class);

        originalUserAccount = response;

        if (originalUserAccount == null) {
            model.addAttribute("info", "Error finding user account");//We pass to edit template error info.
        } else {
            model.addAttribute("originalUserAccount", originalUserAccount);//We pass to edit template originalUserAccount object.
        }

        return "edit";
    }

    @PostMapping("/user_accounts/edit")
    public String postUserAccountEdit(UserAccount userAccount, Model model, HttpServletRequest request) {
        String id = request.getSession().getAttribute("id").toString();//ID of the user account to be edited. We get it from session variables.
        UserAccount modifiedUserAccount = null;//Object returned by IndexPulseAPI.

        //The PUT request is sent to IndexPulseAPI.
        ResponseEntity<UserAccount> response = restClient
                .put()
                .uri("http://localhost:7634/user_accounts/{id}", id)
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(UserAccount.class);

        modifiedUserAccount = response.getBody();//We get the object returned by IndexPulseAPI.

        if (response.getStatusCode().isError()) {
            model.addAttribute("info", "Error modifying user account");//WebController sends "Error posting..." message to template via info variable.
        } else if (modifiedUserAccount == null) {
            model.addAttribute("info", "Error modifying user account");//WebController sends "Error posting..." message to template via info variable.
        } else {
            model.addAttribute("info", "User account modified");//WebController sends "User account..." message to template via info variable.
            id = modifiedUserAccount.getId();//The ID is updated.
            request.getSession().setAttribute("id", id);//We pass to all templates a session variable called id, to know the user id.
        }
        return "test";
    }

    @GetMapping("/index_requests/list")
    public String getIndexRequestList(Model model) {
        ArrayList<IndexRequest> requests = null;//List of all requests made by the logged-in user.
        //Thanks to https://stackoverflow.com/questions/78731216/de-serialize-array-from-restclient-response
        //To get an ArrayList of a JSON array, we declare a new ParameterizedTypeReference.
        ArrayList<IndexRequest> response = restClient.get()
                .uri("http://localhost:7634/index_requests")
                .retrieve()
                .body(new ParameterizedTypeReference<ArrayList<IndexRequest>>() {
                });

        requests = response;//Now requests save all.

        model.addAttribute("requests", requests);

        return "requests";
    }

    @GetMapping("/user_accounts/logout")
    public String getUserAccountLogout(Model model, HttpServletRequest request) {
        request.getSession().setAttribute("id", null);
        model.addAttribute("info", "Logged out successfully");
        return "test";
    }

}
