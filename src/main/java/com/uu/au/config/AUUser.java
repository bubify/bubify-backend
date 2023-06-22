package com.uu.au.config;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AUUser extends org.springframework.security.core.userdetails.User {
    private String token;

    public Long getId() {
        return id;
    }

    private Long id = -1L;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private AUUser(String username) {
        super(username, "unknown", false, false, false, false, AuthorityUtils.createAuthorityList());
    }

    public AUUser(String id, String password, Collection<? extends GrantedAuthority> authorities) {
        super(id, password, authorities);
    }

    public static AUUser create(Long id, String password, Collection<? extends GrantedAuthority> authorities) {
        var newUser = new AUUser(id.toString(), password, authorities);
        knownUsers.put(id, newUser);
        newUser.id = id;
        return newUser;
    }

    public static AUUser createDisabled(String username) {
        return new AUUser(username);
    }

    // FIXME: this feels like replicating existing logic
    public static void remove(Long id) {
        knownUsers.remove(id);
    }

    public static final Map<Long, AUUser> knownUsers = new ConcurrentHashMap<>();
}
