package com.example.ott.service;

import org.springframework.stereotype.Service;

import com.example.ott.dto.UserDTO;
import com.example.ott.entity.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserService {
    

    public UserDTO entityToDto(User user){
        UserDTO userDTO = UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .socials(user.getSocials())
        .userRole(user.getUserRole())
        .mileage(user.getMileage())
        .build();

        return userDTO;
    }
}
