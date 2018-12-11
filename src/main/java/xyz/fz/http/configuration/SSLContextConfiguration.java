package xyz.fz.http.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Configuration
public class SSLContextConfiguration {
    @Value("${ssl.keystore}")
    private String sslKeystore;

    @Value("${ssl.keystore.password}")
    private String sslKeystorePassword;

    @Value("${ssl.enable}")
    private boolean sslEnable;

    @Bean
    public SSLContext sslContext() {
        if (sslEnable) {
            return yesSSL();
        } else {
            return noSSL();
        }
    }

    private SSLContext yesSSL() {
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

    private SSLContext noSSL() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslContext;
    }
}
