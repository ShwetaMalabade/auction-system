package com.database.auction.service.impl;

import com.database.auction.dto.LoginDTO;
import com.database.auction.dto.UsersDTO;
import com.database.auction.entity.Users;
import com.database.auction.exception.UserNotFound;
import com.database.auction.mapper.UsersMapper;
import com.database.auction.repository.UsersRepository;
import com.database.auction.service.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UsersServiceImpl implements UsersService {

    private UsersRepository usersRepository;

    @Override
    public UsersDTO createUsers(UsersDTO usersDTO) {
        Users users = UsersMapper.mapToUsers(usersDTO);
        System.out.println(users.toString());
        Users savedUsers = usersRepository.save(users);

        return UsersMapper.mapToUsersDto(savedUsers);
    }

    @Override
    public UsersDTO getUsers(Integer user_id) {
       Users users =usersRepository.findById(user_id)
               .orElseThrow(() -> new UserNotFound("User Does not exist "+user_id));
       return UsersMapper.mapToUsersDto(users);
    }

    @Override
    public UsersDTO loginUser(LoginDTO loginDTO) {
        System.out.println("Hiii");
        Users users = usersRepository.findByUsername(loginDTO.getUsername()).orElseThrow();
        System.out.println("Finding Username for "+loginDTO.getUsername()+" And "+loginDTO.getPassword_hash());
        if(users.getPassword_hash().equals(loginDTO.getPassword_hash())){
            System.out.println("Yayy");
        } else{
            throw new UserNotFound("Password is not matching for "+loginDTO.getUsername());
        }
        return UsersMapper.mapToUsersDto(users);
    }
}
