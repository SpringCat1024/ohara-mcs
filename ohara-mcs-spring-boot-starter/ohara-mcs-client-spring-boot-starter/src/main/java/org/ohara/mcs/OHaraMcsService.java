package org.ohara.mcs;

import org.ohara.mcs.api.event.EventType;
import org.ohara.mcs.api.grpc.auto.Response;
import org.ohara.mcs.spi.SpiExtensionFactory;
import org.ohara.msc.client.OHaraMcsClient;
import org.ohara.msc.common.log.Log;
import org.ohara.msc.listener.ConfigListener;
import org.ohara.msc.request.Payload;
import org.springframework.beans.factory.DisposableBean;

import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

public class OHaraMcsService implements DisposableBean {

    private final OHaraMcsClient oHaraMcsClient;

    public OHaraMcsService(OHaraMcsClient oHaraMcsClient) {
        SpiExtensionFactory.getExtensions(ConfigListener.class).forEach(ConfigListener::register);
        this.oHaraMcsClient = oHaraMcsClient;
    }

    public Response request(Payload request, EventType eventType) {
        request.setEventType(eventType);
        return oHaraMcsClient.request(request);
    }

    @Override
    public void destroy() {
        if (oHaraMcsClient != null) {
            Log.print("销毁OHaraMcsClient...");
            oHaraMcsClient.destroy(3, TimeUnit.SECONDS);
        }
    }
}