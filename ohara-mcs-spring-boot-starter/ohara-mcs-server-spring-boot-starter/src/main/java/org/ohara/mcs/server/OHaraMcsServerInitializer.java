package org.ohara.mcs.server;

import org.ohara.msc.common.config.OHaraMcsConfig;
import org.ohara.mcs.core.remote.RpcServer;
import org.ohara.mcs.core.remote.raft.RaftServer;
import org.ohara.mcs.server.utils.BannerUtils;
import org.ohara.mcs.spi.SpiExtensionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;


public class OHaraMcsServerInitializer implements ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private final RpcServer rpcServer;

    private final RaftServer raftServer;

    private final BannerUtils bannerUtils;

    private final OHaraMcsConfig config;

    public OHaraMcsServerInitializer(RpcServer rpcServer, OHaraMcsConfig config, BannerUtils bannerUtils) {
        this.config = config;
        this.rpcServer = rpcServer;
        this.bannerUtils = bannerUtils;
        this.raftServer = (RaftServer) SpiExtensionFactory.getExtension(OHaraMcsConfig.RpcType.RAFT.getType(), RpcServer.class);
    }

    @Override
    public void destroy() {
        rpcServer.stop();
        raftServer.stop();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            ApplicationContext ctx = event.getApplicationContext();
            RpcServer rpcServer = ctx.getBean(RpcServer.class);
            rpcServer.start();

            startRaftNode();

            // 如果在 SpringBoot Web 容器环境运行，可以不需要让主线程阻塞
            if (!(ctx instanceof WebApplicationContext)) {
                rpcServer.await();
            }
        }
    }

    private void startRaftNode() {
        raftServer.init(config);
        raftServer.start();
        bannerUtils.print();
    }

}
