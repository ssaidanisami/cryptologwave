package com.cryptolog.wave.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Extract standard scopes (e.g. SCOPE_read, SCOPE_write)
        Collection<GrantedAuthority> defaultAuthorities = defaultGrantedAuthoritiesConverter.convert(jwt);
        if (defaultAuthorities != null) {
            authorities.addAll(defaultAuthorities);
        }

        // 2. Extract roles from standard 'roles' claim
        authorities.addAll(extractRolesFromClaim(jwt, "roles"));

        // 3. Extract roles from 'groups' claim
        authorities.addAll(extractRolesFromClaim(jwt, "groups"));

        // 4. Extract Keycloak 'realm_access.roles'
        authorities.addAll(extractKeycloakRealmRoles(jwt));

        // 5. Extract Keycloak 'resource_access.*.roles'
        authorities.addAll(extractKeycloakResourceRoles(jwt));

        // 6. Extract custom 'authorities' claim if present
        authorities.addAll(extractCustomAuthorities(jwt));

        return Collections.unmodifiableSet(authorities);
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractRolesFromClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim instanceof Collection<?> collection) {
            Set<GrantedAuthority> result = new HashSet<>();
            for (Object item : collection) {
                if (item instanceof String roleName && !roleName.isBlank()) {
                    result.add(createRoleAuthority(roleName));
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractKeycloakRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            Set<GrantedAuthority> result = new HashSet<>();
            for (Object role : roles) {
                if (role instanceof String roleName && !roleName.isBlank()) {
                    result.add(createRoleAuthority(roleName));
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractKeycloakResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return Collections.emptySet();
        }

        Set<GrantedAuthority> result = new HashSet<>();
        for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> clientDetails) {
                Object roles = clientDetails.get("roles");
                if (roles instanceof Collection<?> clientRoles) {
                    for (Object role : clientRoles) {
                        if (role instanceof String roleName && !roleName.isBlank()) {
                            result.add(createRoleAuthority(roleName));
                            // Also add scoped client role e.g. ROLE_CLIENT_ROLE
                            result.add(createRoleAuthority(entry.getKey() + "_" + roleName));
                        }
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractCustomAuthorities(Jwt jwt) {
        Object authoritiesClaim = jwt.getClaims().get("authorities");
        if (authoritiesClaim instanceof Collection<?> collection) {
            Set<GrantedAuthority> result = new HashSet<>();
            for (Object item : collection) {
                if (item instanceof String authName && !authName.isBlank()) {
                    result.add(new SimpleGrantedAuthority(authName));
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    private GrantedAuthority createRoleAuthority(String role) {
        String formattedRole = role.toUpperCase();
        if (!formattedRole.startsWith("ROLE_")) {
            formattedRole = "ROLE_" + formattedRole;
        }
        return new SimpleGrantedAuthority(formattedRole);
    }
}
