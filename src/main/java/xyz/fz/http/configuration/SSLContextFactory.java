package xyz.fz.http.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Component
public class SSLContextFactory {

    @Value("${ssl.keystore}")
    private String sslKeystore;

    @Value("${ssl.keystore.password}")
    private String sslKeystorePassword;

    public SSLContext create() {
        SSLContext sslContext = null;
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getResourceAsStream(sslKeystore);
            if (inputStream == null) {
                throw new RuntimeException("keystore file not found");
            }

            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(inputStream, sslKeystorePassword.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, sslKeystorePassword.toCharArray());

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sslContext;
    }
}
