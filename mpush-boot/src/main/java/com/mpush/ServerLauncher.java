package com.mpush;


import com.mpush.api.Server;
import com.mpush.boot.*;
import com.mpush.core.server.AdminServer;
import com.mpush.core.server.ConnectionServer;
import com.mpush.core.server.GatewayServer;
import com.mpush.tools.config.ConfigCenter;
import com.mpush.zk.ZKServerNode;

/**
 * Created by yxx on 2016/5/14.
 *
 * @author ohun@live.cn
 */
public class ServerLauncher {
    private final ZKServerNode csNode = ZKServerNode.csNode();

    private final ZKServerNode gsNode = ZKServerNode.gsNode();

    private final Server connectServer = new ConnectionServer(csNode.getPort());

    private final Server gatewayServer = new GatewayServer(gsNode.getPort());

    private final Server adminServer = new AdminServer(ConfigCenter.I.adminPort());


    public void start() {
        BootChain chain = BootChain.chain();
        chain.boot()
                .setNext(new RedisBoot())//1.注册redis sever 到ZK
                .setNext(new ZKBoot())//2.启动ZK节点数据变化监听
                .setNext(new ServerBoot(connectServer, csNode))//3.启动长连接服务
                .setNext(new ServerBoot(gatewayServer, gsNode))//4.启动网关服务
                .setNext(new ServerBoot(adminServer, null))//5.启动控制台服务
                .setNext(new HttpProxyBoot())//6.启动http代理服务，解析dns
                .setNext(new MonitorBoot())//7.启动监控
                .setNext(new LastBoot());//8.启动结束
        chain.run();
    }

    public void stop() {
        stopServer(gatewayServer);
        stopServer(gatewayServer);
        stopServer(adminServer);
    }

    private void stopServer(Server server) {
        if (server != null) {
            server.stop(null);
        }
    }
}
