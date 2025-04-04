package org.ohara.msc.lifecycle;


import org.ohara.msc.common.exception.OHaraMcsClientException;

public interface Closeable {
    
    /**
     * Shutdown the Resources, such as Thread Pool.
     *
     * @throws OHaraMcsClientException exception.
     */
    void shutdown() throws OHaraMcsClientException;
    
}