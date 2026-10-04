package br.com.aisaas.auth.application;

import br.com.aisaas.auth.domain.exceptions.InvalidPhoneNumberException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
public class Sanitizer {

    public String sanitizeFullName(String fullName){
        return fullName.trim();
    }

    public Optional<String> sanitizePhoneNumber(String phone){
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();

        try{
            Phonenumber.PhoneNumber number = util.parse(phone, "BR");
            boolean valid = util.isValidNumber(number);

            if (!valid){
                return Optional.empty();
            }
            return Optional.of(util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
        } catch (NumberParseException e) {
            throw new InvalidPhoneNumberException("Invalid number.");
        }
    }

    public String sanitizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
