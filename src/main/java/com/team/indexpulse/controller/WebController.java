package com.team.indexpulse.controller;

import com.team.indexpulse.entity.IndexRequest;
import com.team.indexpulse.entity.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                .toEntity(UserAccount.class);//IndexAPI response.

        if (response.getBody() == null) {//If the account returned by the API equals null:
            model.addAttribute("info", "Error registering user account");//WebController sends "Error posting..." message to template via info variable.
        } else {
            model.addAttribute("info", "User account registered");//WebController sends "User account..." message to template via info variable.
        }

        return "test";//It redirects to test template.
    }

    @PostMapping("/user_accounts/login")
    public String postUserAccountLogin(UserAccount userAccount, Model model, HttpServletRequest request) {
        String userAccountId = "";//ID of user account in case of successful login. It values "" otherwise.

        //The POST request is sent to IndexPulseAPI.
        ResponseEntity<String> response = restClient
                .post()
                .uri("http://localhost:7634/user_accounts/login")
                .contentType(APPLICATION_JSON)
                .body(userAccount)
                .retrieve()
                .toEntity(String.class);//IndexAPI response.

        userAccountId = response.getBody();//userAcountId values the String returned to the previous request.

        request.getSession().setAttribute("id", userAccountId);//We pass to all templates a session variable called id, to know the user id.

        //We pass to "test" template a request variable called info, to show data about the login status:
        if (userAccountId == null) {//If login was not possible:
            model.addAttribute("info", "Login error");
        } else if (userAccountId.isBlank()) {//If login was not possible:
            model.addAttribute("info", "Login error");
        } else {//If login was successful:
            model.addAttribute("info", "Logged in successfully");
        }

        return "test";//It redirects to test template.
    }

    @GetMapping("/index")
    public String getIndex() {
        return "index";//It redirects to index template.
    }

    @GetMapping("/user_accounts/delete")
    public String getUserAccountDelete(Model model, HttpServletRequest request) {
        String id = request.getSession().getAttribute("id").toString();//ID of the user account to be deleted. We get it from session variables.
        ResponseEntity<Void> response = null;//IndexAPI response.
        if (id == null) {
            model.addAttribute("info", "Error deleting user account");//WebController sends "Error deleting..." message to template via info variable.
        } else if (id.isBlank()) {
            model.addAttribute("info", "Error deleting user account");//WebController sends "Error deleting..." message to template via info variable.
        } else {
            //The DELETE request is sent to IndexPulseAPI.
            response = restClient.delete()
                    .uri("http://localhost:7634/user_accounts/{id}", id)
                    .retrieve()
                    .toBodilessEntity();

            model.addAttribute("info", "Account deleted");//WebController sends "Error deleting..." message to template via info variable.
            request.getSession().setAttribute("id", null);//As we've deleted the account, the user is logged out.
        }
        return "test";//It redirects to test template.
    }

    @GetMapping("/user_accounts/edit")
    public String getUserAccountEdit(Model model, HttpServletRequest request) {
        String id = request.getSession().getAttribute("id").toString();//ID of the user account to be edited. We get it from session variables.
        UserAccount originalUserAccount = null;//Object with all original values of the user account.
        UserAccount response = null;//IndexAPI response.

        if (id == null) {
            model.addAttribute("info", "Error getting user account id");//We pass to template error info.
        } else if (id.isBlank()) {
            model.addAttribute("info", "Error getting user account id");//We pass to template error info.
        } else {
            //The GET request is sent to IndexPulseAPI.
            response = restClient.get()
                    .uri("http://localhost:7634/user_accounts/{id}", id)
                    .retrieve()
                    .body(UserAccount.class);

            originalUserAccount = response;

            if (originalUserAccount == null) {
                model.addAttribute("info", "Error finding user account");//We pass to template error info.
            } else {
                model.addAttribute("originalUserAccount", originalUserAccount);//We pass to template originalUserAccount object.
            }
        }

        return "edit";//It redirects to edit template.
    }

    @PostMapping("/user_accounts/edit")
    public String postUserAccountEdit(UserAccount userAccount, Model model, HttpServletRequest request) {
        String userAccountId = request.getSession().getAttribute("id").toString();//ID of the user account to be edited. We get it from session variables.
        UserAccount modifiedUserAccount = null;//Object returned by IndexPulseAPI.
        ResponseEntity<UserAccount> response = null;//IndexAPI response.

        if (userAccountId == null) {
            model.addAttribute("info", "Error getting user account id");//We pass to template error info.
        } else if (userAccountId.isBlank()) {
            model.addAttribute("info", "Error getting user account id");//We pass to template error info.
        } else {
            //The PUT request is sent to IndexPulseAPI.
            response = restClient
                    .put()
                    .uri("http://localhost:7634/user_accounts/{id}", userAccountId)
                    .contentType(APPLICATION_JSON)
                    .body(userAccount)
                    .retrieve()
                    .toEntity(UserAccount.class);

            modifiedUserAccount = response.getBody();//We get the object returned by IndexPulseAPI.

            if (modifiedUserAccount == null) {
                model.addAttribute("info", "Error modifying user account");//WebController sends "Error posting..." message to template via info variable.
            } else {
                model.addAttribute("info", "User account modified");//WebController sends "User account..." message to template via info variable.
                userAccountId = modifiedUserAccount.getId();//The ID is updated.
                request.getSession().setAttribute("id", userAccountId);//We pass to all templates a session variable called id, to know the user id.
            }
        }

        return "test";//It redirects to test template.
    }

    @GetMapping("/index_requests/list")
    public String getIndexRequestList(Model model, HttpServletRequest request) {
        ArrayList<IndexRequest> requests = null;//List of all requests made by the logged-in user.
        String userAccountId = request.getSession().getAttribute("id").toString();//We get the user account id from the logged user.
        ArrayList<IndexRequest> response = null;//IndexAPI response.

        if (userAccountId == null) {
            model.addAttribute("info", "Login error");
        } else if (userAccountId.isBlank()) {
            model.addAttribute("info", "Login error");
        } else {
            //Thanks to https://stackoverflow.com/questions/78731216/de-serialize-array-from-restclient-response
            //To get an ArrayList of a JSON array, we declare a new ParameterizedTypeReference.
            response = restClient.get()
                    .uri("http://localhost:7634/index_requests/{userAccountId}", userAccountId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            requests = response;//Now requests save all.

            model.addAttribute("requests", requests);
        }

        return "requests";//It redirects to requests template.
    }

    @GetMapping("/user_accounts/logout")
    public String getUserAccountLogout(Model model, HttpServletRequest request) {
        request.getSession().setAttribute("id", null);
        model.addAttribute("info", "Logged out successfully");
        return "test";//It redirects to test template.
    }

    @PostMapping("/index_requests/register")
    public String postIndexRequestRegister(IndexRequest indexRequest, Model model, HttpServletRequest request) {
        IndexRequest indexRequestReturned = null;//Index request returned by IndexPulseAPI. If IndexPulseAPI wasn't able to add the index request, this variable values null.
        String userAccountId = request.getSession().getAttribute("id").toString();//We get the user account id from the logged user.
        indexRequest.setUserAccountId(userAccountId);//The request is produced by the user account with this id.
        ResponseEntity<IndexRequest> response = null;//IndexAPI response

        if (userAccountId == null) {
            model.addAttribute("info", "Login error");
        } else if (userAccountId.isBlank()) {
            model.addAttribute("info", "Login error");
        } else {
            //The POST request is sent to IndexPulseAPI.
            response = restClient
                    .post()
                    .uri("http://localhost:7634/index_requests/register")
                    .contentType(APPLICATION_JSON)
                    .body(indexRequest)
                    .retrieve()
                    .toEntity(IndexRequest.class);

            indexRequestReturned = response.getBody();//indexRequestReturned now values the variable returned by IndexPulseAPI.

            if (indexRequestReturned != null) {//If we have received an index request object from POST:
                model.addAttribute("info", "Request added successfully");//WebController sends "Request added..." message to template via info variable.
            } else {//If we haven't received the index request:
                model.addAttribute("info", "Error adding the request");//WebController sends "Error adding..." message to template via info variable.
            }
        }

        return "test";//It redirects to test template.
    }

    @GetMapping("/index_requests/delete/{indexRequestId}")
    public String getIndexRequestDelete(@PathVariable String indexRequestId, Model model, HttpServletRequest request) {
        String userAccountId = request.getSession().getAttribute("id").toString();//We get the user account id from the logged user.
        ArrayList<IndexRequest> getResponse = null;//IndexAPI GET response.
        int i = 0;//While counter.
        boolean found = false;//It values true when we've found that the index request to delete belongs to the user logged in.
        IndexRequest indexRequestIterated = null;
        ResponseEntity<Void> deleteResponse = null;//IndexAPI DELETE response.

        if (userAccountId == null) {
            model.addAttribute("info", "Error deleting request");//WebController sends "Error deleting..." message to template via info variable.
        } else if (userAccountId.isBlank()) {
            model.addAttribute("info", "Error deleting request");//WebController sends "Error deleting..." message to template via info variable.
        } else {
            //Thanks to https://stackoverflow.com/questions/78731216/de-serialize-array-from-restclient-response
            //To get an ArrayList of a JSON array, we declare a new ParameterizedTypeReference.
            getResponse = restClient.get()
                    .uri("http://localhost:7634/index_requests/{userAccountId}", userAccountId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            while (!found && i < getResponse.size()) {//We search the ID to delete in all IDs that belong to the user logged in:
                indexRequestIterated = getResponse.get(i);
                if (indexRequestId.equals(indexRequestIterated.getId())) {
                    found = true;
                }
                i++;
            }

            if (!found) {//If the ID doesn't belong to the user logged in:
                model.addAttribute("info", "Error deleting request");//WebController sends "Error..." message to template via info variable.
            } else {
                //The DELETE request is sent to IndexPulseAPI.
                deleteResponse = restClient.delete()
                        .uri("http://localhost:7634/index_requests/{indexRequestId}", indexRequestId)
                        .retrieve()
                        .toBodilessEntity();

                model.addAttribute("info", "Request deleted");//WebController sends "Request..." message to template via info variable.
            }
        }

        return "requests";//It redirects to requests template.
    }

    @PostMapping("/user_accounts/add_json")
    public String postUserAccountsAddJson(Model model, HttpServletRequest request, MultipartFile file) {
        String userAccountId = request.getSession().getAttribute("id").toString();//We get the user account id from the logged user.
        String uploadDirectory = "C:/Users/jesus/Downloads/uploadExample/" + userAccountId;//File uploading directory. The destination folder is called as the user account ID that uploaded the JSON file.
        Path path = Paths.get(uploadDirectory, file.getOriginalFilename());//Path created for the destination file.
        File directory = new File(uploadDirectory);//directory where the file will be created.
        File[] files = new File(uploadDirectory).listFiles();//Files array to get the current number of files in uploadDirectory. Source: https://stackoverflow.com/questions/31358160/how-to-get-number-of-files-in-a-directory-and-subdirectory-from-a-path-in-java
        int numberOfFiles = 0;//Current number of files in uploadDirectory.

        if (userAccountId == null) {
            model.addAttribute("info", "Error adding JSON");//WebController sends "Error deleting..." message to template via info variable.
        } else if (userAccountId.isBlank()) {
            model.addAttribute("info", "Error adding JSON");//WebController sends "Error deleting..." message to template via info variable.
        } else {
            if (!path.toString().endsWith(".json")) {//If the file received isn't a JSON file:
                model.addAttribute("info", "Error adding JSON");//WebController sends "Error adding..." message to template via info variable.
            } else {//If the file received is a JSON file:
                try {
                    directory.mkdirs();//We create the directory. If it was created before, nothing is done. Source: https://stackoverflow.com/questions/3634853/how-to-create-a-directory-in-java
                    if (files != null) {//If there're files in the directory:
                        numberOfFiles = files.length;//Count the number of files.
                    }
                    if (numberOfFiles >= 10) {//If there are 10 or more files in the directory:
                        model.addAttribute("info", "Error adding JSON");//WebController sends "Error adding..." message to template via info variable.
                    } else {//If there are less than 10 files in the directory:
                        Files.write(path, file.getBytes());//The file is saved to path.
                        model.addAttribute("info", "JSON added successfully");//WebController sends "Request added..." message to template via info variable.
                    }
                } catch (IOException e) {//If there was an I/O problem:
                    model.addAttribute("info", "Error adding JSON");//WebController sends "Error adding..." message to template via info variable.
                }
            }
        }

        return "test";//It redirects to test template.
    }

}