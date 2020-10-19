package com.example.rectifierBackend.config;

import com.example.rectifierBackend.repository.UserRepository;
import com.example.rectifierBackend.security.jwt.JwtAuthEntryPoint;
import com.example.rectifierBackend.security.jwt.JwtAuthTokenFilter;
import com.example.rectifierBackend.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableTransactionManagement
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private UserRepository userRepository;
    private JwtAuthEntryPoint unauthorizedHandler;
    private JwtProvider tokenProvider;

    public WebSecurityConfig(UserRepository userRepository,
                             JwtAuthEntryPoint unauthorizedHandler,
                             JwtProvider tokenProvider) {
        this.userRepository = userRepository;
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter(tokenProvider, userRepository);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService((String username) ->
                userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User Not Found with -> username: " + username)))
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().
                authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/event/**").permitAll()
                .antMatchers("/user/current").permitAll()
                .antMatchers("/user/defaultUser").permitAll()
                .antMatchers("/process/*/report").permitAll()
                .antMatchers("/process/*/report/2").permitAll()
                .antMatchers("/bath/**").hasAnyRole("ADMIN","USER")
                .antMatchers("/process/**").hasAnyRole("ADMIN","USER")
                .antMatchers("/sample/**").hasAnyRole("ADMIN","USER")
                .antMatchers("/user/**").hasRole("ADMIN")
				.anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    }
}
