package com.database.auction.entity;

import com.database.auction.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
//@AllArgsConstructor
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_id",unique = true)
    private int userId;;

    @Column(name = "username",unique = true)
    private String username;

    @Column(name = "password_hash")
    private String password_hash;

    @Column(name = "email", unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private RoleType role;

    public Users(Integer user_id, String username, String password_hash, String email, RoleType role) {
        this.userId = user_id;
        this.username = username;
        this.password_hash = password_hash;
        this.email = email;
        this.role = role;
    }
}
