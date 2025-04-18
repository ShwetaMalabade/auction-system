package com.database.auction.controllers;

import com.database.auction.dto.LoginDTO;
import com.database.auction.dto.UsersDTO;
import com.database.auction.service.UsersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:3000")  // Allow frontend access
@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class UserSignUpController {

    @Autowired
    private UsersService usersService;

    @PostMapping("/signup")
    public ResponseEntity<UsersDTO> createUsers(@RequestBody UsersDTO usersDTO){
        System.out.println("Sign up for Username : "+usersDTO.getUsername());
        System.out.println("The role of User "+usersDTO.getRole());
        return new ResponseEntity<>(usersService.createUsers(usersDTO), HttpStatus.CREATED);
    }

    @GetMapping("{user_id}")
    public ResponseEntity<UsersDTO> getUserbyId(@PathVariable("user_id") Integer id){
       UsersDTO usersDTO = usersService.getUsers(id);
       System.out.println("Searching for the ID  "+usersDTO.getUser_id());
       return ResponseEntity.ok(usersDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<UsersDTO> loginUser(@RequestBody @Valid LoginDTO loginDTO){
        System.out.println("User is logging in");
        return new ResponseEntity<>(usersService.loginUser(loginDTO), HttpStatus.OK);
    }

}
