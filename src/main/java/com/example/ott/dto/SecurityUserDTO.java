package com.example.ott.dto;

import com.example.ott.entity.Socials;
import com.example.ott.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SecurityUserDTO {

    private String id;

    private String name;

    private String nickname;    

    private String email;

    private String password;

    private UserRole userRole;

    private Socials socials;

    // private Struct struct;
}
