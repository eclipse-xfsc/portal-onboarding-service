package tsystems.gaiax.onboarding.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class ProxyCall {

    public static <T> ResponseEntity<T> doGet(final WebClient srv, final HttpServletRequest request) {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((s, strings) -> queryParams.addAll(s, List.of(strings)));

        final WebClient.RequestHeadersSpec<?> callBuilder = srv
                .get()
                .uri(builder ->
                        builder.path(request.getRequestURI())
                                .queryParams(queryParams).build());

        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String hn = headerNames.nextElement();
            callBuilder.header(hn, request.getHeader(hn));
        }

        return callBuilder.retrieve()
                .toEntity(new ParameterizedTypeReference<T>() {
                    //
                }).block();
    }

    public static <R> Flux<DataBuffer> doPostStream(WebClient srv, HttpServletRequest request, R rqBody) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((s, strings) -> queryParams.addAll(s, List.of(strings)));

        WebClient.RequestBodySpec prep = srv
                .post()
                .uri(builder ->
                        builder.path(request.getRequestURI())
                                .queryParams(queryParams).build());

        WebClient.RequestHeadersSpec<?> callBuilder = prep;
        if (rqBody != null) {
            callBuilder = prep.bodyValue(rqBody);
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hn = headerNames.nextElement();
            String header = request.getHeader(hn);
//            System.out.println("HEADER:: " + hn + " -> " + header);
            callBuilder.header(hn, header);
        }

        return callBuilder
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToFlux(DataBuffer.class);
    }

    public static <T, R> ResponseEntity<T> doPost(WebClient srv, HttpServletRequest request, R rqBody) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((s, strings) -> queryParams.addAll(s, List.of(strings)));

        WebClient.RequestBodySpec prep = srv
                .post()
                .uri(builder ->
                        builder.path(request.getRequestURI())
                                .queryParams(queryParams).build());

        WebClient.RequestHeadersSpec<?> callBuilder = prep;
        if (rqBody != null) {
            callBuilder = prep.bodyValue(rqBody);
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hn = headerNames.nextElement();
            String header = request.getHeader(hn);
            System.out.println("HEADER:: " + hn + " -> " + header);
            callBuilder.header(hn, header);
        }
        return callBuilder
                .retrieve()
                .toEntity(new ParameterizedTypeReference<T>() {
                })
                .block();
    }

    public static <T> ResponseEntity<T> doPostFile(WebClient srv, HttpServletRequest request, MultipartFile file) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((s, strings) -> queryParams.addAll(s, List.of(strings)));

        MultipartBodyBuilder mbuilder = new MultipartBodyBuilder();
        mbuilder.part("file", file.getResource());

        WebClient.RequestHeadersSpec<?> callBuilder = srv
                .post()
                .uri(builder ->
                        builder.path(request.getRequestURI())
                                .queryParams(queryParams).build())
                .body(BodyInserters.fromMultipartData(mbuilder.build()));

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hn = headerNames.nextElement();
            String header = request.getHeader(hn);
            System.out.println("HEADER:: " + hn + " -> " + header);
            callBuilder.header(hn, header);
        }
        return callBuilder
                .retrieve()
                .toEntity(new ParameterizedTypeReference<T>() {
                })
                .block();
    }
}
