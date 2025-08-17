package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.User;
import com.mongodb.kitchensink.service.TwoFactorAuthService;
import com.mongodb.kitchensink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorAuthController {

    @Autowired
    private TwoFactorAuthService twoFactorService;

    @Autowired
    private UserService userService;

    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> setup2FA() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userService.getUserByUserName(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String secret = twoFactorService.generateSecretKey();
        user.setTwoFactorSecret(secret);
        userService.saveUser(user);
        String qrCodeURL = twoFactorService.getQRBarcodeURL(username, "YourApp", secret);

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("qrCodeURL", qrCodeURL);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQRCode(@RequestParam String secret) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            String qrCodeURL = twoFactorService.getQRBarcodeURL(username, "YourApp", secret);
            byte[] qrCode = twoFactorService.generateQRCodeImage(qrCodeURL, 200, 200);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify2FA(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String code = request.get("code");

        User user = userService.getUserByUserName(username);
        if (user == null || user.getTwoFactorSecret() == null) {
            return ResponseEntity.badRequest().build();
        }

        boolean isValid = twoFactorService.verifyCode(user.getTwoFactorSecret(), code);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            user.setTwoFactorEnabled(true);
            userService.saveUser(user);
            response.put("message", "2FA enabled successfully");
        } else {
            response.put("message", "Invalid code");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disable2FA(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String code = request.get("code");

        User user = userService.getUserByUserName(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            userService.saveUser(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "2FA disabled successfully");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}