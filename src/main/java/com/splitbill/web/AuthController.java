package com.splitbill.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.splitbill.service.UserService;
import com.splitbill.web.dto.Requests;
import com.splitbill.web.dto.Responses;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }
    @PostMapping("/sign-up") 
    public ResponseEntity<Responses.ApiResponse<Responses.User>> signUp(@Valid @RequestBody Requests.SignUp request) { 
        return ResponseHandler.response(HttpStatus.CREATED, "User signed up successfully", Responses.user(users.signUp(request))); 
    }
    
    @PostMapping("/sign-in") 
    public ResponseEntity<Responses.ApiResponse<Responses.Auth>> signIn(@Valid @RequestBody Requests.SignIn request) { 
        var user = users.signIn(request); 
        return ResponseEntity.ok().header("X-Auth-Token", user.getLoginToken()).body(ResponseHandler.body(HttpStatus.OK, "Signed in successfully", new Responses.Auth(Responses.user(user), user.getLoginTokenExpiration()))); 
    }
}
