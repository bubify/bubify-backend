package com.uu.au;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uu.au.config.AUUser;
import com.uu.au.controllers.AuthController;
import com.uu.au.controllers.UserController;
import com.uu.au.enums.Role;
import com.uu.au.models.User;
import com.uu.au.repository.CourseRepository;
import com.uu.au.repository.UserRepository;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@SpringBootApplication
@EnableScheduling
@EnableCaching(proxyTargetClass = true)
@EnableAspectJAutoProxy(exposeProxy = true)
public class AUPortal {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .setSerializationInclusion(Include.NON_EMPTY)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModule(new JavaTimeModule());

    private static final Logger logger = LoggerFactory.getLogger(AUPortal.class);

    @Value("${server.host.backend}")
    private String BACKEND_HOST_URL;

    @Autowired
    UserRepository userRepository;

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager,
            ServiceProperties serviceProperties) throws Exception {

        logger.info("filter!");

        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager);
        filter.setServiceProperties(serviceProperties);
        return filter;
    }

    @Bean
    public ServiceProperties serviceProperties() {
        logger.info("service properties");
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(BACKEND_HOST_URL + "/login/cas");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    @Bean
    public TicketValidator ticketValidator() {
        logger.info("ticketValidator");
        return new UU_Cas30ServiceTicketValidator("https://weblogin.uu.se/idp/profile/cas");
    }

    @Autowired
    CourseRepository course;

    @Autowired
    UserRepository users;

    @Autowired
    AuthController authController;

    @Autowired
    UserController userController;

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(TicketValidator ticketValidator,
            ServiceProperties serviceProperties) {
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setServiceProperties(serviceProperties);
        provider.setTicketValidator(ticketValidator);
        provider.setUserDetailsService(uuUserName -> {
            var user = authController.installUser(uuUserName);

            if (user.isPresent()) {
                user.get().setToken(authController.keyFromUserId(user.get().getId()));
                var dbUser = users.findByUserNameOrThrow(uuUserName);
                dbUser.setLastLogin(LocalDateTime.now());
                users.save(dbUser);
                return user.get();

            }
            else if (users.count() == 0 && course.count() == 0) {
                var newRootUser = User.builder()
                     .userName(uuUserName)
                     .role(Role.TEACHER)
                     .build();
                userRepository.save(newRootUser);
                user = authController.installUser(uuUserName);
                user.get().setToken(authController.keyFromUserId(user.get().getId()));
                return user.get();
            }
            else {
                logger.error("Created disabled user " + uuUserName);
                return AUUser.createDisabled(uuUserName);
            }
        });
        provider.setKey("CAS_PROVIDER_LOCALHOST_8900");

        logger.info("casAuthenticationProvider!");

        return provider;
    }

    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        logger.info("securityContextLogoutHandler");
        return new SecurityContextLogoutHandler();
    }

    @Bean
    public LogoutFilter logoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter("https://weblogin.uu.se/idp/profile/cas/logout",
                securityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl("/logout/cas");
        return logoutFilter;
    }

    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setLogoutCallbackPath("https://weblogin.uu.se:8443/exit/cas");
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    public static void main(String... args) {
        SpringApplication.run(AUPortal.class, args);
    }
}
