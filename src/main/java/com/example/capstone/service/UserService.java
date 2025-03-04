package com.example.capstone.service;


import com.example.capstone.dto.request.UserDetailsUpdateRequestDto;
import com.example.capstone.dto.response.*;
import com.example.capstone.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;


import com.example.capstone.dto.UserSignupDto;

import com.example.capstone.entity.user.User;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;



    /**
     * 유저 정보 조회
     */
    @Transactional
    public UserDetailsResponseDto getUserDetails(String nick) {

        return UserDetailsResponseDto.createDto(
                userRepository.findByNickname(nick).orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다.")));
    }





    @Transactional
    public void updateUserDetails(String email, UserDetailsUpdateRequestDto userDetailsUpdateRequestDto) throws IOException {

        //
        userRepository.findByUserId(email)
                .orElseThrow()
                .updateDetails(email,userDetailsUpdateRequestDto);

        // 파일이 비어있지 않으면 profile/image 경로에 이미지 저장
        if( userDetailsUpdateRequestDto.getProfile()!=null ) {
            File saveFile = new File("C:/Temp/profile/"+email + ".jpg");
            userDetailsUpdateRequestDto
                    .getProfile()
                    .transferTo(saveFile);
        }
    }


    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUserDetails(String email) {
        userRepository.deleteById(email);
    }



    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 유효성 검사 valid ( id, pw, nickname )
    @Transactional
    public Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }

        return validatorResult;
    }

    // 회원 계정 생성(중복되는 id 찾기)
    public User create(UserSignupDto userDto) throws IOException{
        String id = userDto.getUserId();
        log.info("DTO의 아이디" + id);

        Optional<User> target = userRepository.findById(id);
        log.info("Target" + target);

        target.ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다!");
        });


        //1. Dto -> Entity
        User user = userDto.toEntity();

        //2. repository에게 entity를 디비에 저장하게 시킴
        User saved = userRepository.save(user);

        return saved;

    }

    // 아이디 중복확인
    public boolean existsById(String id) {

        return userRepository.existsById(id);
    }

    // 닉네임 중복확인
    public boolean existsByNickName(String nickname) {

        return userRepository.existsByNickname(nickname);
    }

}

