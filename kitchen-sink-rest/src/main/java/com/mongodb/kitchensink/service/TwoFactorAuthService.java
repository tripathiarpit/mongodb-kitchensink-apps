package com.mongodb.kitchensink.service;

import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

@Service
public class TwoFactorAuthService {

    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    public String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = bytesToHex(bytes);
        return TOTP.getOTP(hexKey);
    }

    public boolean verifyCode(String secretKey, String code) {
        String generatedCode = getTOTPCode(secretKey);
        return generatedCode.equals(code);
    }

    public String getQRBarcodeURL(String user, String host, String secret) {
        return String.format(
                "otpauth://totp/%s@%s?secret=%s&issuer=%s",
                user, host, secret, host
        );
    }

    public byte[] generateQRCodeImage(String barcodeText, int width, int height)
            throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeText,
                BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
