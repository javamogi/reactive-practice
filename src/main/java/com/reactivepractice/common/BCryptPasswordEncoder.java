package com.reactivepractice.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class BCryptPasswordEncoder implements PasswordEncoder{

    private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");
    private final Log logger = LogFactory.getLog(getClass());
    private final int strength;
    private final BCryptVersion version;
    private final SecureRandom random;

    public BCryptPasswordEncoder() {
        this(-1);
    }

    public BCryptPasswordEncoder(int strength) {
        this(strength, null);
    }

    public BCryptPasswordEncoder(BCryptVersion version) {
        this(version, null);
    }

    public BCryptPasswordEncoder(BCryptVersion version, SecureRandom random) {
        this(version, -1, random);
    }

    public BCryptPasswordEncoder(int strength, SecureRandom random) {
        this(BCryptVersion.$2A, strength, random);
    }

    public BCryptPasswordEncoder(BCryptVersion version, int strength) {
        this(version, strength, null);
    }

    public BCryptPasswordEncoder(BCryptVersion version, int strength, SecureRandom random) {
        if (strength != -1 && (strength < 4 || strength > 31)) {
            throw new IllegalArgumentException("Bad strength");
        }
        this.version = version;
        this.strength = (strength == -1) ? 10 : strength;
        this.random = random;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        String salt = getSalt();
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    private String getSalt() {
        if (this.random != null) {
            return BCrypt.gensalt(this.strength, this.random);
        }
        return BCrypt.gensalt(this.strength);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        if (encodedPassword == null || encodedPassword.length() == 0) {
            this.logger.warn("Empty encoded password");
            return false;
        }
        if (!this.BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            this.logger.warn("Encoded password does not look like BCrypt");
            return false;
        }
        return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
    }

    public enum BCryptVersion {

        $2A("$2a"),

        $2Y("$2y"),

        $2B("$2b");

        private final String version;

        BCryptVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return this.version;
        }

    }

}
