package pl.finder.elmer.io;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public interface SslConfigurator {
    SslConfigurator sslContext(SSLContext context);

    SslConfigurator trustManager(TrustManager trustManager);
}