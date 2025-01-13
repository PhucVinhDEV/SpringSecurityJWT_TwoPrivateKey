package com.example.BitzNomad.Util;

import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
@Getter
public class KeyUtil {

    private static final String KEYSTORE_PATH = "keystore.p12";
    private static final String KEYSTORE_PASSWORD = "123123a";
    private static final String KEY_ALIAS = "bitznomad";

    public static final PrivateKey privateKey;
    public static final PublicKey publicKey;

    static {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }

            // Lấy private key từ keystore
            privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());

            // Lấy certificate và extract public key từ keystore
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(KEY_ALIAS);
            publicKey = certificate.getPublicKey();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể nạp cặp khóa từ KeyStore", e);
        }
    }


}
