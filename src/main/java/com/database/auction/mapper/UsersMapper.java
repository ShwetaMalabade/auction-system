package com.database.auction.mapper;

import com.database.auction.dto.UsersDTO;
import com.database.auction.entity.Users;

public class UsersMapper {

    public static UsersDTO mapToUsersDto(Users users){
        return new UsersDTO(
                users.getUserId(),
                users.getUsername(),
                users.getPassword_hash(),
                users.getEmail(),
                users.getRole()
        );
    }

    public static Users mapToUsers(UsersDTO usersDTO){
        return new Users(
                usersDTO.getUser_id(),
                usersDTO.getUsername(),
                usersDTO.getPassword_hash(),
                usersDTO.getEmail(),
                usersDTO.getRole()
        );
    }


}
