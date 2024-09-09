package com.scaler.userservice.services;

import com.scaler.userservice.models.Token;
import com.scaler.userservice.models.User;
import com.scaler.userservice.repositories.TokenRepository;
import com.scaler.userservice.repositories.UserRepository;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User signUp(String name, String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()) {
            // TODO: Throw an exception from here like UserAlreadyExists
            return null;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(user);
    }

    @Override
    public Token login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()) {
            // TODO: Throw an exception that user does not exist
            return null;
        }

        User user = userOptional.get();
        if(!bCryptPasswordEncoder
                .matches(password, user.getHashedPassword())) {
            // TODO: throw an exception that password is wrong
            return null;
        }

        Token token = createToken(user);
        return tokenRepository.save(token);
    }

    @Override
    public User validate(String tokenValue) {
        Optional<Token> tokenOptional = tokenRepository
                .findByValueAndDeletedAndExpiryAtGreaterThan(tokenValue,
                        false,
                        new Date());

        if(tokenOptional.isEmpty()) {
            // TODO: throw an exception TokenInvalidException
            return null;
        }

        Token token = tokenOptional.get();

        return token.getUser();
    }

    @Override
    public void logout(String token) {
        // HW
    }

    private Token createToken(User user) {
        Token token = new Token();
        token.setUser(user);
        token.setValue(RandomStringUtils.randomAlphanumeric(128));

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date dateAfter30Days = calendar.getTime();

        token.setExpiryAt(dateAfter30Days);
        token.setDeleted(false);

        return token;
    }
}
