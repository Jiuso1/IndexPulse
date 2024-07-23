package com.team.indexpulse.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import com.team.indexpulse.entity.UserAccount;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@org.springframework.stereotype.Controller
public class WebController {

    private final RestClient restClient = RestClient.create();

    @PostMapping("/user_accounts/register")
    public String postUserAccountRegister(UserAccount userAccount, Model model) {
        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<Void> response = restClient.post()
                .uri("http://localhost:7634/user_accounts/register")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().isError()) {
            model.addAttribute("info", "Error registering user account");//WebController sends "Error posting..." message to template via info variable.
        } else {
            model.addAttribute("info", "User account registered");//WebController sends "User account..." message to template via info variable.
        }

        return "test";
    }

    @PostMapping("/user_accounts/login")
    public String postUserAccountLogin(UserAccount userAccount, Model model, HttpServletRequest request) {
        Boolean login = false;//Controls whether the user is logged in or not.

        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<Boolean> response = restClient.post()
                .uri("http://localhost:7634/user_accounts/login")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(Boolean.class);

        login = response.getBody();//login values the Boolean returned to the previous request.

        request.getSession().setAttribute("login", login);//We pass to all templates a session variable called login, to control if the user is logged in.

        //We pass to "test" template a request variable called info, to show data about the login status:
        if (!login) {//If login was not possible:
            model.addAttribute("info", "Login error");
        } else {//If login was successful:
            model.addAttribute("info", "Logged in");
        }

        return "test";
    }

    @GetMapping("/index")
    public String getIndex(HttpServletRequest request) {
        return "index";
    }

}
