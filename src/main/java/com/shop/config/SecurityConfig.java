package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http.formLogin()
                .loginPage("/members/login") // 로그인 페이지 URL
                .defaultSuccessUrl("/") // 로그인 성공시 이동할 URL
                .usernameParameter("email") // 로그인 시 사용할 파라미터
                .failureUrl("/members/login/error") // 로그인 실패시 이동할 URL
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) // 로그아웃 URL
                .logoutSuccessUrl("/"); // 로그아웃 성공시 이동할 URL

        http.authorizeRequests() // 시큐리티 처리에 HttpServletRequest를 이용
                .mvcMatchers("/", "/members/**",
                        "/item/**", "/images/**").permitAll() // permitAll을 통해 모든 사용자가 인증 없이 해당경로에 접근
                .mvcMatchers("/admin/**").hasRole("ADMIN") // /admin으로 시작하는 경로는 ADMIN role일 경우에만 접근 가능
                .anyRequest().authenticated(); // 설정해준 경로 외 나머지는 모두 인증 요구
        http.exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint()); // 인증되지 않은 사용자가 리소스에 접근하였을 때 수행되는 핸들러 등록
    }

    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); //해시 함수를 이용하여 비밀번호를 암호화하여 저장
    }

    // Spring Security에서 인증은 AuthenticationManager를 통해 이루어지며 AuthenticationManagerBuilder가 AuthenticationManager를 생성한다.
    // userDetailService를 구현하고 있는 객체로 memberService를 지정해주며, 비밀번호 암호화를 위해 passwordEncoder를 지정해준다.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(memberService).passwordEncoder(passwordEncoder()); // static 디렉터리의 하위 파일을 인증을 무시하도록 설정
    }
}
