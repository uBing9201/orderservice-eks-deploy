package com.playdata.userservice.common.auth;

import com.playdata.userservice.user.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 클라이언트가 전송한 토큰을 검사하는 필터
// 스프링 시큐리티에 등록해서 사용할 겁니다.
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 게이트웨이가 토큰 내에 클레임을 헤더에 담아서 보내준다.
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");
        log.info("userEmail:{} userRole:{}", userEmail, userRole);

        if (userEmail != null && userRole != null) {

            // spring security에게 전달할 인가 정보 리스트를 생성. (권한 정보)
            // 권한이 여러 개 존재할 경우 리스트로 권한 체크에 사용할 필드를 add. (권한 여러개면 여러번 add 가능)
            // 나중에 컨트롤러의 요청 메서드마다 권한을 파악하게 하기 위해 미리 저장을 해 놓는 것.
            List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
            // ROLE_USER, ROLE_ADMIN (ROLE_ 접두사는 필수입니다.
            // 나중에 role 꺼내올 때 시큐리티가 ROLE_를 붙여서 검색을 합니다.
            authorityList.add(new SimpleGrantedAuthority("ROLE_" + userRole));

            // 인증 완료 처리
            // 위에서 준비한 여러가지 사용자 정보, 인가정보 리스트를 하나의 객체로 포장
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    new TokenUserInfo(userEmail, Role.valueOf(userRole)), // 컨트롤러 등에서 활용할 유저 정보
                    "", // 인증된 사용자의 비밀번호: 보통 null 혹은 빈 문자열로 선언.
                    authorityList // 인가 정보 (권한)
            );

            // 시큐리티 컨테이너에 인증 정보 객체를 등록.
            // 인증 정보를 전역적으로 어느 컨테이너, 어느 서비스에서나 활용할 수 있도록 미리 저장.
            SecurityContextHolder.getContext().setAuthentication(auth);

        }
        // 필터를 통과하는 메서드 (doFilter를 호출하지 않으면 필터 통과가 안됩니다!)
        // if (token != null) 바깥쪽으로 뺐습니다.
        // 일단 토큰이 있든 없든 필터를 통과해서 시큐리티한테 검사는 받아야 하니깐...
        filterChain.doFilter(request, response);

    }
}











