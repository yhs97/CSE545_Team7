package com.sbs.sbsgroup7.security;

import com.sbs.sbsgroup7.DataSource.UserRepository;
import com.sbs.sbsgroup7.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class userDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    public userDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(userName));

        user.orElseThrow(() -> new UsernameNotFoundException("Not Found: "+ userName));

        return user.map(userDetailsImpl::new).get();
    }
}