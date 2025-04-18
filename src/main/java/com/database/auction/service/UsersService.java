package com.database.auction.service;


import com.database.auction.dto.LoginDTO;
import com.database.auction.dto.UsersDTO;

public interface UsersService {
    UsersDTO createUsers(UsersDTO usersDTO);

    UsersDTO getUsers(Integer user_id);

    UsersDTO loginUser(LoginDTO loginDTO);

    //UsersDTO loginUsers()
}
