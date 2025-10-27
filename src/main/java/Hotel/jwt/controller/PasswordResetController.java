package Hotel.jwt.controller;


import Hotel.jwt.dto.auth.ForgotPasswordRequest;
import Hotel.jwt.dto.auth.ResetPasswordRequest;
import Hotel.jwt.dto.common.ApiResponse;
import Hotel.jwt.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService resetPasswordService;

    @PostMapping(value="/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest req) {

        resetPasswordService.generateAndSendToken(req.email());

        return ResponseEntity.ok(
                ApiResponse.ok(null, "Si el correo existe, te enviamos un enlace.")
        );
    }


    @PostMapping(value="/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        boolean ok = resetPasswordService.resetPassword(req.token().trim(), req.newPassword());
        if (ok) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contraseña actualizada correctamente."
            ));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Token inválido, expirado o ya utilizado. Solicita uno nuevo."
        ));
    }
}