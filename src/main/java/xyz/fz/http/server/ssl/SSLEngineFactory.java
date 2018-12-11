package xyz.fz.http.server.ssl;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Properties;

public class SSLEngineFactory {

    private static SSLContext sslContext;

    static {
        initSslContext();
    }

    private static void initSslContext() {
        InputStream inputStream = null;
        try {
            Properties applicationProperties = PropertiesLoaderUtils.loadAllProperties("application.properties");
            String sslKeystore = applicationProperties.getProperty("ssl.keystore");
            inputStream = SSLEngineFactory.class.getResourceAsStream(sslKeystore);
            if (inputStream == null) {
                throw new RuntimeException("keystore file not found");
            }
            String sslKeystorePassword = applicationProperties.getProperty("ssl.keystore.password");

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
    }

    public static SSLEngine create() {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        return sslEngine;
    }
}
