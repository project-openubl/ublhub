package io.github.project.openubl.ublhub.models;

import com.github.f4b6a3.tsid.TsidFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ThreadLocalRandom;

public class TsidUtil {

    @ConfigProperty(name = "ublhub.tsid.bytes")
    int tsidBytes;

    @Produces
    @ApplicationScoped
    public TsidFactory provideTsidFactory() {
        int nodeBits = (int) (Math.log(tsidBytes) / Math.log(2));

        return TsidFactory.builder()
                .withRandomFunction(length -> {
                    final byte[] bytes = new byte[length];
                    ThreadLocalRandom.current().nextBytes(bytes);
                    return bytes;
                })
                .withNodeBits(nodeBits)
                .build();
    }

}
