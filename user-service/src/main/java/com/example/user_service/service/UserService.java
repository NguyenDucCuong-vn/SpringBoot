package com.example.user_service.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Cacheable("allUsers")
    public List<User> getAllUsers() {
        System.out.println("Quering from the database");
        return userRepository.findAll(); 
    }
}
