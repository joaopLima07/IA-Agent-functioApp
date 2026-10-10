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

    /** Spends the same time as a real check, so unknown e-mails can't be told apart by response time. */
    public void burnPasswordCheck(String passwordSent){
        passwordEncoder.matches(passwordSent, dummyHash);
    }

    public String validPassword(String password){

        if (password.length() < 8){
            throw new WeakPasswordException("The password is less then 8 caracters");
        }

        boolean haveText = Pattern.compile("[a-zA-Z]").matcher(password).find();

        if (!haveText){
            throw new WeakPasswordException("The password dosen`t have text.");
        }

        boolean haveNumber = Pattern.compile("\\d").matcher(password).find();

        if (!haveNumber){
            throw new WeakPasswordException("The password dosen`t have number.");
        }

        boolean haveSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        if (!haveSpecial){
            throw new WeakPasswordException("The password dosen`t have special caracter.");
        }

        return passwordEncoder.encode(password);

    }

    public boolean equalsPassword(User user, String passwordSent){
        return passwordEncoder.matches(passwordSent, user.getPassword());
    }
}
