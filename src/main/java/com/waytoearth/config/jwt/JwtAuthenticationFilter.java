package com.waytoearth.config.jwt;

import com.waytoearth.entity.enums.UserRole;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.auth.JwtTokenProvider;
import com.waytoearth.service.auth.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
// postman 프로파일에서도 JWT 인증 사용하도록 변경
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 블랙리스트 체크 (로그아웃된 토큰 차단)
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("블랙리스트 토큰 접근 차단");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            UserRole role = jwtTokenProvider.getRoleFromToken(token);

            if (userId != null) {
                // AuthenticatedUser 객체 생성
                AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, role);

                // GrantedAuthority 생성 (Spring Security 권한)
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(role.getKey())
                );

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(authenticatedUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("인증 성공: userId = {}, role = {}", userId, role);
            } else {
                log.warn("JWT 토큰에서 사용자 ID를 추출할 수 없습니다");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}