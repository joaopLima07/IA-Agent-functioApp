package br.com.aisaas.auth.application;

import br.com.aisaas.auth.api.dto.LoginRequest;
import br.com.aisaas.auth.api.dto.LoginResponse;
import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.InvalidCredentialsException;
import br.com.aisaas.auth.infra.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {
    private static final String INVALID_CREDENTIALS = "Invalid e-mail or password.";

    private final UserRepository userRepository;
    private final Sanitizer sanitizer;
    private final PasswordPolicy passwordPolicy;
    private final JwtTokenService jwtTokenService;

    public LoginService(UserRepository userRepository, Sanitizer sanitizer, PasswordPolicy passwordPolicy, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.sanitizer = sanitizer;
        this.passwordPolicy = passwordPolicy;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest loginRequest){

        String sanitizedEmail = sanitizer.sanitizeEmail(loginRequest.email());

        Optional<User> haveUser = userRepository.findByEmail(sanitizedEmail);

        if (haveUser.isEmpty()){
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        boolean equalPassword = passwordPolicy.equalsPassword(haveUser.get(), loginRequest.password());

        if (!equalPassword){
            throw new InvalidCredentialsException(INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenService.generateToken(haveUser.get());

        return new LoginResponse(accessToken, jwtTokenService.getExpirationSeconds());
    }
}
