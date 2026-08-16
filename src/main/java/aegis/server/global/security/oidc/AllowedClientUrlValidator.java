package aegis.server.global.security.oidc;

import java.net.URI;

import org.springframework.stereotype.Component;

import static aegis.server.global.constant.Constant.ALLOWED_CLIENT_URLS;

@Component
public class AllowedClientUrlValidator {

    public boolean isAllowed(String url) {
        URI uri = parseHttpUri(url);
        if (uri == null) {
            return false;
        }

        return ALLOWED_CLIENT_URLS.stream()
                .map(this::parseHttpUri)
                .anyMatch(allowedUri -> hasSameOrigin(uri, allowedUri));
    }

    private URI parseHttpUri(String url) {
        if (url == null) {
            return null;
        }

        try {
            URI uri = URI.create(url);
            if (!uri.isAbsolute()
                    || uri.getHost() == null
                    || uri.getRawUserInfo() != null
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean hasSameOrigin(URI uri, URI allowedUri) {
        return allowedUri != null
                && uri.getScheme().equalsIgnoreCase(allowedUri.getScheme())
                && uri.getHost().equalsIgnoreCase(allowedUri.getHost())
                && effectivePort(uri) == effectivePort(allowedUri);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
