package com.shop.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//위 어노테이션이 달린 클래스에 @Bean 어노테이션이 붙은 메서드를
//등록하면 해당 메서드의 반환 값이 스프링 빈으로 등록됨
@EnableWebSecurity
public class SecurityConfig {

    /* 스프링 시큐리티 필터 체인 */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin((it) -> it
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
        );

//        http.csrf(AbstractHttpConfigurer::disable);

        http.logout(logout -> logout
                .logoutUrl("/members/logout")
                .logoutSuccessUrl("/")
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/members/**", "/item/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.exceptionHandling((e) -> e
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        return http.build();
    }

    //화면을 정상적으로 그리는데 필요한 정적인 자원들 허용
    //특별히 보안적인 고려사항이 없는 웹 전용 자원들
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
