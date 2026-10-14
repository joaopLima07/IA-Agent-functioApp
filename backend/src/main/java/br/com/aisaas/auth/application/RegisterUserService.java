package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.EmailAlreadyRegisteredException;
import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import br.com.aisaas.auth.infra.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegisterUserService {
    private final UserRepository registerUserRepository;
    private final PasswordPolicy passwordPolicy;
    private final Sanitizer sanitize;

    public RegisterUserService(UserRepository registerUserRepository, PasswordPolicy passwordPolicy, Sanitizer sanitize) {
        this.registerUserRepository = registerUserRepository;
        this.passwordPolicy = passwordPolicy;
        this.sanitize = sanitize;
    }

    public User createUser(RegisterUserCommand userCommand){

        String sanitizedFullName = sanitize.sanitizeFullName(userCommand.getFullName());
        String sanitizedEmail = sanitize.sanitizeEmail(userCommand.getEmail());
        String hashPassword = passwordPolicy.validPassword(userCommand.getPassword());
        Optional<String> sanitizedPhone = sanitize.sanitizePhoneNumber(userCommand.getPhone());

        if (sanitizedPhone.isEmpty()){
            throw new InvalidPhoneNumberException("O número de telefone informado é inválido.");
        }

        User user = new User();

        user.setFullName(sanitizedFullName);
        user.setEmail(sanitizedEmail);
        user.setPassword(hashPassword);
        user.setPhone(sanitizedPhone.get());

        Optional<User> haveEmail = registerUserRepository.findByEmail(user.getEmail());

        if (haveEmail.isPresent()){
            throw new EmailAlreadyRegisteredException("Este e-mail já está registrado.");
        }

        return registerUserRepository.save(user);
    }

    
}
