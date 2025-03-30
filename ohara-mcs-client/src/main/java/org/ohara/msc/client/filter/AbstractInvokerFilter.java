package org.ohara.msc.client.filter;


import org.ohara.mcs.api.grpc.auto.Response;
import org.ohara.msc.client.AbstractClient;
import org.ohara.msc.client.invoke.AbstractInvoker;
import org.ohara.msc.common.enums.ResponseCode;
import org.ohara.msc.common.exception.OHaraMcsClientException;
import org.ohara.msc.common.log.Log;
import org.ohara.mcs.api.result.ResponseHelper;
import org.ohara.msc.context.OHaraMcsContext;
import org.ohara.msc.option.RequestOption;
import org.ohara.msc.request.Payload;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractInvokerFilter<OPTION extends RequestOption> extends AbstractFilter<OPTION> {

    private final ConcurrentHashMap<String, AbstractInvoker<?, OPTION>> invokers = new ConcurrentHashMap<>();

    protected AbstractInvokerFilter(AbstractClient<OPTION> client) {
        super(client);
    }

    @Override
    protected Response doPreFilter(OHaraMcsContext context, OPTION option, Payload request) {
        try {

            return getInvoker(option.protocol()).invoke(context, request);
        } catch (Exception ex) {
            String errMsg = MessageFormat.format("[Client-Request] Invoke request fail, {0}", ex.getMessage());
            Log.error(errMsg, ex);
            return ResponseHelper.error(ResponseCode.SYSTEM_ERROR.getCode(), errMsg);
        }

    }

    @Override
    protected Response doPostFilter(OHaraMcsContext context, OPTION option, Payload request, Response response) {
        return response;
    }

    protected AbstractInvoker<?, OPTION> getInvoker(String protocol) {
        AbstractInvoker<?, OPTION> invoker = this.invokers.get(protocol);
        if (invoker == null) {
            throw new OHaraMcsClientException(MessageFormat.format("Unidentified protocol {0}", protocol));
        }
        return invoker;
    }

    @SuppressWarnings("all")
    protected void registerInvoker(AbstractInvoker invoker) {
        this.invokers.put(invoker.protocol(), invoker);
    }
}
