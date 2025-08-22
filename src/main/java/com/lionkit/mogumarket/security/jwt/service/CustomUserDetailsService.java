package com.lionkit.mogumarket.security.jwt.service;




import com.lionkit.mogumarket.security.oauth2.principal.PrincipalDetails;
import com.lionkit.mogumarket.user.entity.User;
import com.lionkit.mogumarket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new PrincipalDetails(user);
    }

    /**
     * CustomUserDetailsService 을 사용하기 위해서 loadUserByUsername 은 필수 구현이기에 구현해놓았습니다.
     * 실제 코드에서는 loadUserByUsername 대신 loadUserById 를 활용합니다.
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new PrincipalDetails(user);
    }
}
