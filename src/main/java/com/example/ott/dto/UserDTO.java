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
public class UserDTO {

    private String id;

    private String name;

    private String email;

    private UserRole userRole;

    private Socials socials;

    private Long mileage = 0L;

    // private Struct struct;
}
