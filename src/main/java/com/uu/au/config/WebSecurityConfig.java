package com.uu.au.config;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collections;

import static org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private SingleSignOutFilter singleSignOutFilter;
    @Autowired
    private LogoutFilter logoutFilter;
    @Autowired
    private CasAuthenticationProvider casAuthenticationProvider;
    @Autowired
    private ServiceProperties serviceProperties;

    @Autowired
    private Environment environment;

    @Value("${server.host.frontend}")
    private String FRONTEND_HOST_URL;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/secured").authenticated().and().exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint()).and()
                .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class).logout().addLogoutHandler(foo())
                .deleteCookies().clearAuthentication(true).invalidateHttpSession(true).logoutUrl("/logout")
                .logoutSuccessUrl(FRONTEND_HOST_URL).logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .and().addFilterBefore(logoutFilter, LogoutFilter.class).csrf().ignoringAntMatchers("/exit/cas");

        /// Protect all admin endpoints from being accessed outside of the deployment machine
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDevelopmentProfileActive = false;

        for (String profile : activeProfiles) {
            if (profile.equals("development")) {
                isDevelopmentProfileActive = true;
                break;
            }
        }

        if (!isDevelopmentProfileActive) {
            http.authorizeRequests().antMatchers("/admin/**", "/su", "/internal/**").access("hasIpAddress('127.0.0.1') or hasIpAddress('::1')");
        }

        http.authorizeRequests().antMatchers("/error").permitAll();

        http.cors().and().csrf().disable().authorizeRequests().anyRequest().permitAll();

        http.sessionManagement().maximumSessions(2).and().sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }

    LogoutHandler foo() {
        return (request, response, authentication) -> {
            logger.info("Logging out " + authentication.getPrincipal());
            var x = (AUUser) authentication.getPrincipal();
            AUUser.remove(x.getId());

            CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(
                    SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
            cookieClearingLogoutHandler.logout(request, response, authentication);

            request.getSession().invalidate();
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider);
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return new ProviderManager(Collections.singletonList(casAuthenticationProvider));
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        CasAuthenticationEntryPoint entryPoint = new CasAuthenticationEntryPoint();
        entryPoint.setLoginUrl("https://weblogin.uu.se/idp/profile/cas/login");
        entryPoint.setServiceProperties(serviceProperties);
        return entryPoint;
    }

}
