package com.example.ott.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.example.ott.dto.ImageDTO;
import com.example.ott.entity.Image;
// import com.example.ott.dto.ReviewDTO;
// import com.example.ott.entity.Member;
import com.example.ott.entity.Movie;
// import com.example.ott.entity.Review;
import com.example.ott.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
@Service
public class ImageService {

    // @Autowired
    // private ImageRepository imageRepository;

    // public FileSystemResource getImageFile(Integer inum) {
    // Image image = imageRepository.findByInum(inum)
    // .orElseThrow(() -> new RuntimeException("Image not found"));

    // return new FileSystemResource(image.getPath());
    // }
}