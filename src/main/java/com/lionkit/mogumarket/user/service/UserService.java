package com.lionkit.mogumarket.user.service;

import com.lionkit.mogumarket.cart.entity.Cart;
import com.lionkit.mogumarket.cart.service.CartService;
import com.lionkit.mogumarket.global.base.response.exception.BusinessException;
import com.lionkit.mogumarket.global.base.response.exception.ExceptionType;
import com.lionkit.mogumarket.point.entity.Point;
import com.lionkit.mogumarket.point.service.PointService;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.dto.request.SignUpRequestDto;
import com.lionkit.mogumarket.user.dto.response.UserResponseDto;
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
    private final CartService cartService;



    public UserResponseDto signup(SignUpRequestDto request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException(ExceptionType.DUPLICATED_USERNAME);
        }

        // 비밀번호 암호화 및 저장
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        User user = request.toEntity();
        user = userRepository.save(user);

        // 회원가입과 함께 포인트 wallet 생성
        Point point = pointService.createPointWallet(user.getId());

        // 회원가입과 함꼐 cart 생성
        Cart cart = cartService.createOrGetCart(user.getId());

        return UserResponseDto.fromEntity(user, point.getId(),cart.getId());
    }


    public UserResponseDto getUserInfoById(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(()-> new BusinessException(ExceptionType.USER_NOT_FOUND));

        Long pointId = pointService.getSnapshot(user.getId()).getId();
        Long cartId = cartService.createOrGetCart(user.getId()).getId();

        return UserResponseDto.fromEntity(user, pointId ,cartId);
    }




}