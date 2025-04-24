package com.database.auction.repository;

import com.database.auction.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, String> {
    UserDetails findByUsername(String username);
}
