package com.lionkit.mogumarket.user.service;

import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.point.entity.Point;
import com.lionkit.mogumarket.point.service.PointService;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.dto.request.SignUpRequestDto;
import com.lionkit.mogumarket.user.dto.response.SignUpResponseDto;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointService pointService;



    public SignUpResponseDto signup(SignUpRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ExceptionType.DUPLICATED_USERNAME);
        }

        // 비밀번호 암호화 및 저장
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User user = request.toEntity();
        user = userRepository.save(user);

        // 회원가입과 함께 포인트 wallet 생성
        Point point = pointService.createPointWallet(user.getId());

        return SignUpResponseDto.fromEntity(user, point.getId());
    }






}