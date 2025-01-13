package com.example.BitzNomad.Service;

import com.example.BitzNomad.DTO.AuthenticationResponse;
import com.example.BitzNomad.DTO.IntrospecResponsee;
import com.example.BitzNomad.Entity.InvalidatedToken;
import com.example.BitzNomad.Entity.User;
import com.example.BitzNomad.Repository.InvalidatedTokenRepository;
import com.example.BitzNomad.Repository.UserRepository;
import com.example.BitzNomad.Util.KeyUtil;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
@Slf4j
public class JWTService {


    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @Autowired
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Autowired
    UserRepository userRepository;

    public String generateJWT(User user) {
        try {
            // Tạo header với thuật toán RSA
            JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

            // Xây dựng claims set
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plus(3600, ChronoUnit.SECONDS))) // Hết hạn sau 1 giờ
                    .jwtID(UUID.randomUUID().toString())
                    .claim("scope",buildScope(user))
                    .build();

            // Tạo JWT đã ký
            SignedJWT signedJWT = new SignedJWT(header, jwtClaimsSet);

            // Ký JWT với private key sử dụng RSASSA (RSA với SHA-256)
            signedJWT.sign(new RSASSASigner(KeyUtil.privateKey));

            // Trả về JWT đã ký dạng chuỗi
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("JWT Exception", e);
            throw new RuntimeException("JWT generation failed", e);
        }
    }
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().stream().filter(permission -> !permission.isDeleted()).forEach(permission -> stringJoiner.add(permission.getPermissionName()));
            });
        return stringJoiner.toString();
    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        // Sử dụng RSASSAVerifier cho RSA
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) KeyUtil.publicKey);

        // Phân tích token
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Xác định thời gian hết hạn
        Date expirationTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                        .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        // Xác thực chữ ký
        boolean verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(Date.from(Instant.now())))) {
            throw new RuntimeException("Signature verification failed");
        }

        // Kiểm tra xem token có bị vô hiệu hóa không
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new RuntimeException("JWT invalidated");
        }

        return signedJWT;
    }

    public AuthenticationResponse refeshToken(String token) throws ParseException, JOSEException {
        var SingJWT = verifyToken(token,true);

        var jit = SingJWT.getJWTClaimsSet().getJWTID();

        var expirationTime = SingJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expirationTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);

        var u = SingJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findById(u).orElseThrow(
                () -> new RuntimeException("User created JWT not found")
        );


        String tokenNew = generateJWT(user);
        return  AuthenticationResponse.builder()
                .token(tokenNew)
                .authenticated(true)
                .build();
    }
    public void logout(String token) throws ParseException, JOSEException {
        try {
            SignedJWT signToken = verifyToken(token,false);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        }catch (JOSEException exception){
            log.info("Token already expired");
        };
    }

    public IntrospecResponsee Instropec(String token) throws JOSEException, ParseException {

        boolean isValid = true;

        try{
            verifyToken(token,false);
        }catch (RuntimeException exception){
            isValid = false;
        }

        return IntrospecResponsee.builder()
                .valid(isValid)
                .build();
    }
}
