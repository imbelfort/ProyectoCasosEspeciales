/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

/**
 *
 * @author Usuario
 */
public class ConfigEmailServer {
    public static final String PORT_SMTP = getConfig("MAIL_SMTP_PORT", "25");
    public static final String PORT_POP3 = getConfig("MAIL_POP3_PORT", "110");
    public static final String PORT_IMAP = getConfig("MAIL_IMAP_PORT", "143");
    public static final String PROTOCOL = getConfig("MAIL_PROTOCOL", "smtp");
    public static final String HOST = getConfig("MAIL_HOST", "mail.tecnoweb.org.bo");
    public static final String USER = getConfig("MAIL_USER", "grupo01sa");
    public static final String PASSWORD = getConfig("MAIL_PASSWORD", "grup001grup001*");
    public static final String MAIL = getConfig("MAIL_ADDRESS", "grupo01sa@tecnoweb.org.bo");
    public static final String MAIL_PASSWORD = getConfig("MAIL_PASSWORD", "grup001grup001*");

    private static String getConfig(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return defaultValue;
    }
}
