package pt.ipleiria.estg.dei.ei.dae.academics.security;

import io.jsonwebtoken.Jwts;
import pt.ipleiria.estg.dei.ei.dae.academics.entities.User;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TokenIssuer {
    protected static final byte[] SECRET_KEY =
            "veRysup3rstronginvincible5ecretkey@academics.dae.ipleiria".getBytes();
    protected static final String ALGORITHM = "HMACSHA384";
    public static final long EXPIRY_MINS = 60L;

    public static String issue(User user) {
        LocalDateTime expiryPeriod = LocalDateTime.now().plusMinutes(EXPIRY_MINS);
        Date expirationDateTime = Date.from(
                expiryPeriod.atZone(ZoneId.systemDefault()).toInstant()
        );

        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(expirationDateTime)
                .signWith(key)
                .compact();
    }

    public static byte[] getSecretKey() {
        return SECRET_KEY;
    }

    public static String getAlgorithm() {
        return ALGORITHM;
    }
}