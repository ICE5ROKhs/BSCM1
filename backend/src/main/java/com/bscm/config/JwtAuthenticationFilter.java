package com.bscm.config;

import com.bscm.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        if (jwtUtil.validateToken(token)) {
          String phone = jwtUtil.getPhoneFromToken(token);
          Long userId = jwtUtil.getUserIdFromToken(token);

          if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    phone,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("JWT认证成功，用户: {}, 请求路径: {}", phone, request.getRequestURI());
          } else {
            log.debug("JWT认证失败：phone为null或已存在认证信息，phone: {}", phone);
          }
        } else {
          log.warn("JWT token验证失败，请求路径: {}", request.getRequestURI());
        }
      } catch (Exception e) {
        log.error("JWT认证处理异常，请求路径: {}", request.getRequestURI(), e);
      }
    } else {
      log.debug("请求缺少Authorization header或格式不正确，请求路径: {}", request.getRequestURI());
    }

    filterChain.doFilter(request, response);
  }
}
