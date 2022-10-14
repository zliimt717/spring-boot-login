package com.example.springbootlogin.registration;

import com.example.springbootlogin.appuser.AppUser;
import com.example.springbootlogin.appuser.AppUserRole;
import com.example.springbootlogin.appuser.AppUserService;
import com.example.springbootlogin.registration.token.ConfirmationToken;
import com.example.springbootlogin.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;

    public String register(RegistrationRequest request) {

       boolean isValidEmail= emailValidator.test(request.getEmail());

       if(!isValidEmail){
           throw new IllegalStateException("email not valid");
       }
         String token = appUserService.signUpUser(
                        new AppUser(
                            request.getFirstName(),
                            request.getLastName(),
                            request.getEmail(),
                            request.getPassword(),
                            AppUserRole.USER

                        )
        );
        String link="http://localhost:8080/api/v1/registration/confirm?token="+token;
        return token;
    }


    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken=confirmationTokenService
                .getToken(token)
                .orElseThrow(
                        ()->new IllegalStateException("token not found")
                );
        if(confirmationToken.getConfirmedAt()!=null){
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt=confirmationToken.getExpiresAt();

        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);

        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail()
        );

        return "confirmed";
    }

}
