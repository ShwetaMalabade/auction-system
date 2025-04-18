package com.database.auction.repository;

import com.database.auction.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    public Optional<Users> findByUsername(String Username);
    Users findByUserId(int userId);
}
