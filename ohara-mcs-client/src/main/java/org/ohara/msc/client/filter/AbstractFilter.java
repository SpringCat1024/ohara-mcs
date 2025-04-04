package org.ohara.msc.client.filter;

import org.ohara.mcs.api.grpc.auto.Response;
import org.ohara.msc.client.AbstractClient;
import org.ohara.msc.common.exception.InitializationException;
import org.ohara.msc.common.exception.OHaraMcsClientException;
import org.ohara.msc.common.exception.OHaraMcsException;
import org.ohara.msc.common.log.Log;
import org.ohara.msc.context.OHaraMcsContext;
import org.ohara.msc.option.RequestOption;
import org.ohara.msc.request.Payload;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

public abstract class AbstractFilter<OPTION extends RequestOption> implements Filter<Response, OPTION> {

    private Filter<Response, OPTION> pre;

    private Filter<Response, OPTION> next;

    private final AbstractClient<OPTION> client;

    protected AbstractFilter(AbstractClient<OPTION> client) {
        this.client = client;
    }

    public OPTION getOption() {
        return (OPTION) client.getOption();
    }

    @Override
    public void init() throws Exception {

        Log.info("[Client-Filter-Start] Initialize Filter {}", this.getClass().getCanonicalName());

        try {
            doInit();
        } catch (Exception ex) {
            Log.error("[Client-Filter-Start] Initialize Filter fail, {}", ex.getMessage(), ex);
            throw new InitializationException(ex);
        }

        if (this.next() != null) {
            this.next().init();
        }
    }

    @Override
    public Response preFilter(OHaraMcsContext context, OPTION option, Payload request) {
        Response response = doPreFilter(context, option, request);
        return this.next() == null || response != null
                // End: execute post filter
                ? postFilter(context, option, request, response)
                // Continue: next filter.
                : this.next().preFilter(context, option, request);
    }

    @Override
    public Response postFilter(OHaraMcsContext context, OPTION option, Payload request, Response response) {
        Response resp = doPostFilter(context, option, request, response);
        return this.pre() == null
                // End: return response.
                ? resp
                // Continue: last filter.
                : this.pre().postFilter(context, option, request, resp);
    }

    @Override
    public void destroy(Integer timeout, TimeUnit unit) {

        Log.info("[Client-Filter-Destroy] Destroy Filter {}", this.getClass().getCanonicalName());
        try {
            doDestroy(timeout, unit);
        } catch (Exception ex) {
            Log.error("[Client-Filter-Destroy] Destroy Filter fail, {}", ex.getMessage(), ex);
        }

        if (this.next() != null) {
            this.next().destroy(timeout, unit);
        }

    }

    @Override
    public Filter<Response, OPTION> next(Filter<Response, OPTION> filter) {
        Log.debug("[Client-Filter-Start] Add next filter to FilterChain, pre: {} -> next: {}",
                this.getClass().getCanonicalName(), filter.getClass().getCanonicalName());

        this.next = filter;
        filter.pre(this);
        return this;
    }

    @Override
    public Filter<Response, OPTION> next() {
        return this.next;
    }

    @Override
    public Filter<Response, OPTION> pre(Filter<Response, OPTION> filter) {
        this.pre = filter;
        return this;
    }

    @Override
    public Filter<Response, OPTION> pre() {
        return this.pre;
    }

    protected void doInit() throws Exception {
    }

    protected void doDestroy(Integer timeout, TimeUnit unit) {
    }

    protected abstract Response doPreFilter(OHaraMcsContext context, OPTION option, Payload request);

    protected abstract Response doPostFilter(OHaraMcsContext context, OPTION option, Payload request, Response response);
}
