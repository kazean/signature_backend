package com.example.memorydb.user.service;

import com.example.memorydb.user.db.UserRepository;
import com.example.memorydb.user.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserEntity save(UserEntity user) {
        // save
        return userRepository.save(user);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public void delete(Long id) {
        UserEntity userEntity = userRepository.findById(id).get();
        userRepository.delete(userEntity);
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<UserEntity> filetScore(int score) {
//        return userRepository.findAllScoreGraterThan(score);
        return userRepository.findAllByScoreGreaterThanEqual(score);
    }

    public List<UserEntity> filetScore(int min, int max) {
//        return userRepository.findAllByScoreBetween(min, max);
        return userRepository.score(min, max);
    }

}
