package com.team.indexpulse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import com.team.indexpulse.entity.UserAccount;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@org.springframework.stereotype.Controller
public class WebController {

    private final RestClient restClient = RestClient.create();

    @PostMapping("/user_accounts")
    public String postUserAccount(UserAccount userAccount) {
        userAccount.setId(UUID.randomUUID().toString());//We generate the unique ID before posting it.

        //Attribute debugging:
        System.out.println(userAccount.getId());
        System.out.println(userAccount.getEmail());
        System.out.println(userAccount.getPassword());
        System.out.println(userAccount.getFirstName());
        System.out.println(userAccount.getLastName());

        System.out.println("Posting user account");

        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<Void> response = restClient.post()
                .uri("http://localhost:7634/user_accounts")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toBodilessEntity();

        if(response.getStatusCode().isError()){
            System.out.println("Error posting user account");
        }else{
            System.out.println("User account posted");
        }

        return "test";
    }

}
