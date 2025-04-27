package com.database.auction.controllers;

import com.database.auction.dto.LoginDTO;
import com.database.auction.dto.PasswordDTO;
import com.database.auction.dto.ProfileDTO;
import com.database.auction.dto.UsersDTO;
import com.database.auction.service.UsersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
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

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ProfileDTO> getProfile(
            @PathVariable int userId) {

        ProfileDTO profile = usersService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /auth/editprofile/{userId}
     */
    @PutMapping("/editprofile/{userId}")
    public ResponseEntity<ProfileDTO> editProfile(
            @PathVariable int userId,
            @RequestBody ProfileDTO profileDto) {

        ProfileDTO updated = usersService.updateProfileByUserId(userId, profileDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value="/pwd_change/{userId}",consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> pwd_Change(
            @PathVariable int userId,
            @RequestBody PasswordDTO password_hash)
    {
        System.out.println("In Controller");
        log.info("pass" + password_hash);
        int rows= usersService.pwd_Change(userId, password_hash.getPassword_hash());
        //return  usersService.pwd_Change(userId, password_hash);

        if(rows>0)
            return ResponseEntity.status(HttpStatus.CREATED).build();

        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();



    }




}
