package ex.rr.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepo;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        Optional<User> user = userRepo.findByUsername(authentication.getName());
        if (user.isPresent()) {
            if (user.get().getPassword().equals(authentication.getCredentials().toString())) {
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
