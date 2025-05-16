package gateway.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// 요청이 모든 필터를 거친 후, 즉 응답 직전에 실행되는 필터
@Slf4j
@Component
public class PostFilter implements GlobalFilter, Ordered {

    /**
     * GlobalFilter: 모든 요청에 대해 작동하는 공통 필터 인터페이스.
     * Ordered: 필터의 실행 순서를 지정할 수 있도록 해주는 인터페이스.
     */

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            log.info("Post Filter: Response status code is " + response.getStatusCode());
            //모든 필터 처리 후(요청이 서비스로 전달되고 응답이 돌아온 뒤)에 실행될 로직.
            //.then()은 비동기 처리가 끝난 후 실행되는 후처리(Post) 동작.
            //Mono.fromRunnable(...)은 비동기 코드가 끝나고 Runnable(리턴 없는 작업) 을 실행하게 된다.
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
