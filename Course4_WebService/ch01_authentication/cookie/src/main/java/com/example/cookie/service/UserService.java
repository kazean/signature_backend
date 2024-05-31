package com.example.cookie.service;

import com.example.cookie.db.UserRepository;
import com.example.cookie.model.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public String login(LoginRequest loginRequest,
                      HttpServletResponse httpServletResponse) {
        var id = loginRequest.getId();
        var password = loginRequest.getPassword();

        var optionalUser = userRepository.findByName(id);
        if (optionalUser.isPresent()) {
            var userDto = optionalUser.get();
            if (userDto.getPassword().equals(password)) {
                return userDto.getId();
            }
        } else {
            throw new RuntimeException("User Not Found");
        }

        return null;
    }
}
