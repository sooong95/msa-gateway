package gateway.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        switch (path) {
            case "/member/join" -> {
                return chain.filter(exchange); // 해당 경로는 필터를 적용하지 않는다.
            }
            case "/auth/login" -> {
                return chain.filter(exchange);
            }
            case "/member/test" -> {
                return chain.filter(exchange);
            }
            case "/" -> {
                return chain.filter(exchange);
            }
        }

        String token = extractToken(exchange);

        if (token == null || !validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
            // 토큰이 없거나 유효하지 않으면 401 Unauthorized 상태로 응답을 종료
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        /**
         * secretKey 를 디코딩하여 HMAC SHA 서명용 키를 생성.
         * Jwts.parser().verifyWith(key).build().parseSignedClaims(token):
         * JWT 의 서명 검증과 Claims 파싱을 한 번에 수행.
         * claimsJws.getPayload(): 토큰 본문의 claim(이메일, 권한 등)을 로깅.
         * 예외가 발생하면 유효하지 않은 토큰으로 간주하고 false 를 반환.
         */

        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
            Jws<Claims> claimsJws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            log.info("payload = {}", claimsJws.getPayload().toString());

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
