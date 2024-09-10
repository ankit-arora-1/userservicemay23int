package com.scaler.userservice.services;

import com.scaler.userservice.models.Token;
import com.scaler.userservice.models.User;
import com.scaler.userservice.repositories.TokenRepository;
import com.scaler.userservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SecretKey secretKey;

    public UserServiceImpl(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.secretKey = Keys.hmacShaKeyFor("myveryveryverylongsecretkey123456789".getBytes(StandardCharsets.UTF_8));
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

        Token token = createJwtToken(user);

        return tokenRepository.save(token);
    }

    @Override
    public User validate(String tokenValue) {
        Jws<Claims> claims = Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(tokenValue);

        if(claims == null) {
            return null;
        }

        Date expiryAt = claims.getPayload().getExpiration();
        Long userId = claims.getPayload().get("userId", Long.class);
        String email = claims.getPayload().get("email", String.class);

        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        return user;
    }

    @Override
    public void logout(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(tokenValue, false);

        if (optionalToken.isEmpty()) {
            //Throw some exception
            return;
        }

        Token token = optionalToken.get();

        token.setDeleted(true);
        tokenRepository.save(token);
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

    private Token createJwtToken(User user) {
        Map<String, Object> dataInJwt = new HashMap<>();
        dataInJwt.put("userId", user.getId());
        dataInJwt.put("email", user.getEmail());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date datePlus30Days = calendar.getTime();

        String jwt = Jwts.builder()
                .claims(dataInJwt)
                .expiration(datePlus30Days)
                .issuedAt(new Date())
                .signWith(secretKey)
                .compact();

        Token token = new Token();
        token.setValue(jwt);
        token.setUser(user);
        token.setExpiryAt(datePlus30Days);
        token.setDeleted(false);

        return token;
    }
}
