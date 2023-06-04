package com.security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class CustomJdbcDriver implements Driver {

    private static final String JDBC_PREFIX = "jdbc:postgres";
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static String SECRET_KEY;
    private static final Logger LOGGER = Logger.getLogger(CustomJdbcDriver.class.getName());

    static {
        try {
            SECRET_KEY = System.getProperty("DECRYPT_KEY");
            SECRET_KEY = "YourSecretKey123"; //TODO: Change this to get from system env
            System.clearProperty("DECRYPT_KEY");
            DriverManager.registerDriver(new CustomJdbcDriver());
        } catch (SQLException e) {
            throw new RuntimeException("Error registering Custom JDBC driver");
        }
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        String decryptedUrl = decryptConnectionString(url);
        Driver delegateDriver = findDelegateDriver(decryptedUrl);
        properties.replaceAll((key, value) -> {
            try {
                return decryptText((String) value);
            } catch (Exception e) {
                return null;
            }
        });
        return delegateDriver.connect(decryptedUrl, properties);
    }

    public Driver findDelegateDriver(String url) throws SQLException {

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.acceptsURL(url) && !(driver instanceof CustomJdbcDriver)) {
                return driver;
            }
        }
        return null;
    }

    private String decryptConnectionString(String encryptedConnectionString) {
        String decryptedConnectionString = encryptedConnectionString;
        return decryptedConnectionString;
    }

    public static String decryptText(String encryptedText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    @Override
    public boolean acceptsURL(String s) {
        return s.startsWith(JDBC_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
        return DriverManager.getDriver(decryptConnectionString(s)).getPropertyInfo(decryptConnectionString(s), properties);
    }

    @Override
    public int getMajorVersion() {
        return 4;
    }

    @Override
    public int getMinorVersion() {
        return 2;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() {
        return LOGGER.getParent();
    }

    /*
    Just for reference

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String SECRET_KEY = "YourSecretKey123";

    private static String encryptText(String rawText) throws Exception {

        SecretKeySpec secretKeySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(rawText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);

    }
     */
}
