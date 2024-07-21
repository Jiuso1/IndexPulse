package com.team.indexpulse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import com.team.indexpulse.entity.UserAccount;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@org.springframework.stereotype.Controller
public class WebController {

    private final RestClient restClient = RestClient.create();

    @PostMapping("/user_accounts/register")
    public String postUserAccountRegister(UserAccount userAccount) {
        userAccount.setId(UUID.randomUUID().toString());//We generate the unique ID before posting it.

        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<Void> response = restClient.post()
                .uri("http://localhost:7634/user_accounts/register")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().isError()) {
            System.out.println("Error posting user account");
        } else {
            System.out.println("User account posted");
        }

        return "test";
    }

    @PostMapping("/user_accounts/login")
    public String postUserAccountLogin(UserAccount userAccount, Model model) {
        Boolean login = false;
        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<Boolean> response = restClient.post()
                .uri("http://localhost:7634/user_accounts/login")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(Boolean.class);

        login = response.getBody();

        System.out.println("The value received is " + login);

        model.addAttribute("login",login);//The controller sends login variable to the web template (test).

        return "test";
    }

}
