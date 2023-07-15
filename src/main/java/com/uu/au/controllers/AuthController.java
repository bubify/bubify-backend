package com.uu.au.controllers;

import com.uu.au.config.AUUser;
import com.uu.au.enums.errors.UserErrors;
import com.uu.au.repository.UserRepository;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Key;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;


@Controller
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final Map<String, Pair<LocalTime, String>> unAuthorizedKeys = new ConcurrentHashMap<>();
    private final Map<String, Pair<LocalTime, String>> authorizedKeys = new ConcurrentHashMap<>();

    @Autowired
    UserRepository users;

    private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private Key devKey = Keys.hmacShaKeyFor("development_keyjidwfniowdjiojewdioewdewdedwoij".getBytes());
    @Autowired
    public Environment environment;

    private Key getKey() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDevelopmentProfileActive = false;

        for (String profile : activeProfiles) {
            if (profile.equals("development")) {
                isDevelopmentProfileActive = true;
                break;
            }
        }

        if (isDevelopmentProfileActive) {
            return devKey;
        } else {
            return key;
        }
    }

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void deleteStaleKeys() {
        logger.info("Pruning stale keys");

        /// Delete all keys older than 5 minutes
        var cullingTime = LocalTime.now().minusMinutes(5);

        var entriesToDelete = unAuthorizedKeys
                .keySet()
                .stream()
                .filter(k -> unAuthorizedKeys.get(k).getFirst().isBefore(cullingTime))
                .collect(Collectors.toList());
        for (var key : entriesToDelete) {
            unAuthorizedKeys.remove(key);
        }

        var entriesToDelete2 = authorizedKeys
                .keySet()
                .stream()
                .filter(k -> authorizedKeys.get(k).getFirst().isBefore(cullingTime))
                .collect(Collectors.toList());
        for (var key : entriesToDelete2) {
            authorizedKeys.remove(key);
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/auth")
    public RedirectView auth(@RequestParam String id, RedirectAttributes redirectAttrs) {

        var timeToLive = LocalTime.now().plusMinutes(3);
        unAuthorizedKeys.put(id, Pair.of(timeToLive, "TODO"));
        redirectAttrs.addAttribute("staging", id);
        return new RedirectView("/secured");
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/consume-token")
    public @ResponseBody String consumeToken(@RequestParam String id) {
        if (authorizedKeys.containsKey(id)) {
            var data = authorizedKeys.remove(id);

            return data.getFirst().isAfter(LocalTime.now())
                    ? data.getSecond()
                    : "key-expired";
        } else {
            /// No valid key found
            return "error-no-such-key";
        }
    }

    @GetMapping("/unsuccessful")
    public @ResponseBody String loginFailed() {
        return "Your login attempt failed -- please report this to the head teacher.";
    }

    @GetMapping("/error")
    public @ResponseBody String error() {
        return "\"ERROR\"";
    }

    @GetMapping("/unregistered")
    public @ResponseBody String unregistered() {
        return "Your username is not registered for this course.";
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/secured")
    public @ResponseBody String securedIndex(@RequestParam String staging, HttpServletRequest request) {
        var expiryDate = Optional.ofNullable(unAuthorizedKeys.remove(staging));

        if (expiryDate.isEmpty() || expiryDate.get().getFirst().isBefore(LocalTime.now())) {
            /// For some reason, we came here without having first gone through /auth
            logger.info("Successful redirect from CAS, but request time too old");
            return "Your login took too long -- the session has expired.";

        } else {
            /// All is well
            logger.info("Successful redirect from CAS");

            var auUser =(AUUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            /// Add exception to AUPortal to handle unregistered users

            logger.info("Associating key with id '" + auUser.getUsername() + "'");

            HttpSession session = request.getSession(true);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());


            try {
                /// Refresh the token in the user object
                auUser.setToken(keyFromUserId(auUser.getId()));

                authorizedKeys.put(staging, Pair.of(expiryDate.get().getFirst(), auUser.getToken()));
                // authorizedKeys.put(staging, Pair.of(expiryDate.getFirst(), encrypt(key.get(), expiryDate.getSecond())));
                return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title></title><script type=\"text/javascript\">window.close();</script></head><body>Please close this window</script></body></html>";

            } catch (Exception e) {

                logger.error(e.getMessage());
                throw UserErrors.malformedUserName("WRONG ERROR"); /// FIXME: replace with better error
            }
        }
    }

    public String keyFromUserId(long uid) {
        var issuedTime = System.currentTimeMillis();
        return Jwts.builder().setSubject(Long.toString(uid)).setIssuedAt(new Date(issuedTime)).setIssuer("com.uu.au").signWith(getKey()).setExpiration(new Date(issuedTime+60*1000*60*12*4)).compact(); //48 hours expiration
    }

    public String keyFromUserIdWithAudience(long uid, String audience) {
        return Jwts.builder().setSubject(Long.toString(uid)).setAudience(audience).signWith(getKey()).compact();
    }

    private Optional<String> generateKey() {
        try {
            var uuUserName =((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

            final var user = users.findByUserName(uuUserName)
                    .orElseThrow(UserErrors::userNotFound);

            final AUUser auUser = AUUser.knownUsers.containsKey(user.getId())
                    ? AUUser.knownUsers.get(user.getId())
                    : AUUser.create(user.getId(), "<Handled_by_CAS>", AuthorityUtils.createAuthorityList(user.getRole().getRole()));

            final var usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(auUser, null, auUser.getAuthorities());
            //usernamePasswordAuthenticationToken
            //        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            return Optional.ofNullable(keyFromUserId(user.getId())); // token
        } catch (JwtException e) {
            /// FIXME: supress for now
            logger.info("JwtException: " + e.getMessage());

        } catch (Exception e) {
            /// FIXME: supress for now
            logger.info("Exception: " + e.getMessage());
        }

        return Optional.empty();
    }


    public AUUser installUserFromToken(String token) {
        try {
            var username = Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody().getSubject();
            var userId = Long.parseLong(username);
            return installUser(userId).orElseThrow(() -> UserErrors.malformedUserName(token));
        } catch (ExpiredJwtException e) {
            throw new JwtException(e.getMessage());
        }
        catch (JwtException e) {
            throw new JwtException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new JwtException("Could not extract sub from token " + token);
        }
    }

    public Optional<AUUser> installUser(String uuUserName) {
        final var user = users.findByUserName(uuUserName);
        return user.flatMap(u -> installUser(u.getId()));
    }

    public Optional<AUUser> installUser(long id) {
        var user = users
                .findById(id)
                .map(value -> AUUser
                        .create(id, "<Handled_by_CAS>",
                                AuthorityUtils.
                                        createAuthorityList(value.getRole().getRole())));
        user.ifPresent(auUser -> auUser.setToken(keyFromUserId(auUser.getId())));
        return user;
    }
}
