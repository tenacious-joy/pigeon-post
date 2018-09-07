
package com.pigeon.post.messaging.messagingdemo.services;

import com.pigeon.post.messaging.messagingdemo.model.Account;
import com.pigeon.post.messaging.messagingdemo.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findAccountByUsername(username);
        if(account != null) {
            List<GrantedAuthority> authorities =
                    new ArrayList<GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new org.springframework.security.core.userdetails.User(
                    account.getUsername(),
                    account.getAuthId(),
                    authorities
            );

        }
        throw new UsernameNotFoundException("User '"+username+"' not found");

    }

}
