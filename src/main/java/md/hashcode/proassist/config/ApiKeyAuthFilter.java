package md.hashcode.proassist.config;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";
    private final Set<String> configuredKeys;

    public ApiKeyAuthFilter(org.springframework.core.env.Environment env) {
        // IMPORTANT: property name must match your YAML: proassist.security.api-keys
        String raw = env.getProperty("proassist.security.api-keys", "");
        this.configuredKeys = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        log.info("ApiKeyAuthFilter initialized. keysConfigured={}", this.configuredKeys.size());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Only protect /api/**
        if (!path.startsWith("/api/")) return true;

        // Allow preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // Allow public photo GETs so <img src="..."> works without headers
        return path.startsWith("/api/phones/") && path.contains("/photos/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {

            String provided = request.getHeader(HEADER);
            if (provided == null) {
                log.warn("Missing API key. {} {}", method, uri);
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API key");
                return;
            }

            boolean ok = configuredKeys.stream().anyMatch(k -> constantTimeEquals(k, provided));
            if (!ok) {
                log.warn("Invalid API key. {} {}", method, uri);
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API key");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // This guarantees you will see the root cause
            log.error("ApiKeyAuthFilter failure for {} {}", method, uri, ex);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Auth filter failure");
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}
