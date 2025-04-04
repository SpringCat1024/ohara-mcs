package org.ohara.msc.remote.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Message;
import org.ohara.mcs.api.grpc.auto.MetadataDeleteRequest;
import org.ohara.mcs.api.grpc.auto.MetadataReadRequest;
import org.ohara.mcs.api.grpc.auto.MetadataWriteRequest;
import org.ohara.mcs.api.grpc.auto.Response;
import org.ohara.mcs.spi.Join;
import org.ohara.msc.common.config.OHaraMcsConfig;
import org.ohara.msc.common.exception.OHaraMcsClientException;
import org.ohara.msc.common.exception.OHaraMcsException;
import org.ohara.msc.dto.ServerAddress;
import org.ohara.msc.future.RequestFuture;
import org.ohara.msc.remote.RpcClient;
import org.ohara.msc.utils.ServerAddressConverter;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Join(order = 1, isSingleton = true)
public class GrpcClient extends GrpcConnection implements RpcClient<Message, Response> {

    /**
     * for spi load
     */
    public GrpcClient() {
        super();
    }

    public GrpcClient(String namespace, List<ServerAddress> serverAddresses) {
        super(namespace, serverAddresses);
    }

    @Override
    public Response request(Message request) {
        if (request instanceof MetadataWriteRequest) {
            return put((MetadataWriteRequest) request);
        } else if (request instanceof MetadataDeleteRequest) {
            return delete((MetadataDeleteRequest) request);
        } else if (request instanceof MetadataReadRequest) {
            return get((MetadataReadRequest) request);
        }
        throw new OHaraMcsClientException("GrpcClient unsupported request type: " + request.getClass().getName());
    }

    @Override
    public RequestFuture<Response> requestFuture(Message request) {
        if (request instanceof MetadataWriteRequest) {
            return putAsync((MetadataWriteRequest) request);
        } else if (request instanceof MetadataDeleteRequest) {
            return deleteAsync((MetadataDeleteRequest) request);
        } else if (request instanceof MetadataReadRequest) {
            return getAsync((MetadataReadRequest) request);
        }
        return null;
    }

    public Response put(MetadataWriteRequest request) {
        return blockingStub.put(request);
    }

    public Response delete(MetadataDeleteRequest request) {
        return blockingStub.delete(request);
    }

    public Response get(MetadataReadRequest request) {
        return blockingStub.get(request);
    }

    public RequestFuture<Response> deleteAsync(MetadataDeleteRequest request) throws OHaraMcsException{
        ListenableFuture<Response> future = futureStub.delete(request);
        return createRequestFuture(future);
    }

    public RequestFuture<Response> getAsync(MetadataReadRequest request) throws OHaraMcsException {
        ListenableFuture<Response> future = futureStub.get(request);
        return createRequestFuture(future);
    }

    public RequestFuture<Response> putAsync(MetadataWriteRequest request) throws OHaraMcsException {
        ListenableFuture<Response> future = futureStub.put(request);
        return createRequestFuture(future);
    }

    private RequestFuture<Response> createRequestFuture(ListenableFuture<Response> future) {
        return new RequestFuture<>() {
            @Override
            public boolean isDone() {
                return future.isDone();
            }

            @Override
            public Response get() throws Exception {
                Response response = future.get();
                handleResponse(response);
                return response;
            }

            @Override
            public Response get(long timeout) throws Exception {
                Response response = future.get(timeout, TimeUnit.MILLISECONDS);
                handleResponse(response);
                return response;
            }
        };
    }

    private void handleResponse(Response response) throws OHaraMcsException {
        if (!response.getSuccess()) {
            throw new OHaraMcsException(response.getMsg());
        }
    }
}
