package ex.rr.tasklist.security;

import ex.rr.tasklist.database.entity.Role;
import ex.rr.tasklist.database.entity.User;
import ex.rr.tasklist.database.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepo;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Optional<User> user = userRepo.findByUsername(authentication.getName());
        if (user.isPresent()) {
            if (BCrypt.checkpw(authentication.getCredentials().toString(), user.get().getPassword())) {
                List<GrantedAuthority> authorities = new ArrayList<>();
                for (Role role : user.get().getRoles()) {
                    authorities.add(new SimpleGrantedAuthority(role.getRole()));
                }
                return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), authorities);
            } else {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), null);
                authenticationToken.setAuthenticated(false);
                return authenticationToken;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
