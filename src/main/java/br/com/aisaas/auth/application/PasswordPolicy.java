package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.User;
import br.com.aisaas.auth.domain.exceptions.WeakPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordPolicy {
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public PasswordPolicy(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    public void burnPasswordCheck(String passwordSent){
        passwordEncoder.matches(passwordSent, dummyHash);
    }

    public String validPassword(String password){

        if (password.length() < 8){
            throw new WeakPasswordException("A senha deve ter pelo menos 8 caracteres.");
        }

        boolean haveText = Pattern.compile("[a-zA-Z]").matcher(password).find();

        if (!haveText){
            throw new WeakPasswordException("A senha deve conter uma letra.");
        }

        boolean haveNumber = Pattern.compile("\\d").matcher(password).find();

        if (!haveNumber){
            throw new WeakPasswordException("A senha deve conter um número.");
        }


        return passwordEncoder.encode(password);

    }

    public boolean equalsPassword(User user, String passwordSent){
        return passwordEncoder.matches(passwordSent, user.getPassword());
    }
}
