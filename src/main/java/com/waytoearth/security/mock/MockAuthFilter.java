package com.waytoearth.security.mock;

import com.waytoearth.entity.enums.UserRole;
import com.waytoearth.security.AuthenticatedUser; //  진짜 principal import
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Profile("postman-disabled") // 비활성화: postman에서 실제 JWT 토큰 방식 사용
public class MockAuthFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = Optional.ofNullable(request.getServletPath()).orElse("");
        return p.startsWith("/v3/api-docs")
                || p.startsWith("/swagger-ui")
                || p.startsWith("/actuator")
                || p.equals("/health")
                || p.equals("/ping");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // 기본값은 1L, 헤더로 오버라이드 가능
            Long userId = Optional.ofNullable(request.getHeader("X-Mock-UserId"))
                    .filter(s -> !s.isBlank())
                    .map(Long::valueOf)
                    .orElse(1L);

            // Mock role (헤더로 ADMIN 설정 가능)
            UserRole role = Optional.ofNullable(request.getHeader("X-Mock-Role"))
                    .filter(s -> !s.isBlank())
                    .map(UserRole::valueOf)
                    .orElse(UserRole.USER);

            AuthenticatedUser principal = new AuthenticatedUser(userId, role);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority(role.getKey()))
                    );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
