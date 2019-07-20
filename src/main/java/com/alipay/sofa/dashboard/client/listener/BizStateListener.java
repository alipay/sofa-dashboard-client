/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.dashboard.client.listener;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.springboot2.endpoint.IntrospectBizEndpoint;
import com.alipay.sofa.dashboard.client.common.Constants;
import com.alipay.sofa.dashboard.client.common.NetworkAddressUtils;
import com.alipay.sofa.dashboard.client.common.ObjectBytesUtils;
import com.alipay.sofa.dashboard.client.config.SofaDashboardProperties;
import com.alipay.sofa.dashboard.client.zookeeper.ZkCommandClient;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/7/16 7:53 PM
 * @since:
 **/
public class BizStateListener {

    private static final Logger           LOGGER = LoggerFactory.getLogger(BizStateListener.class);

    private final ZkCommandClient         zkCommandClient;

    private final SofaDashboardProperties sofaDashboardProperties;

    private final String                  appName;

    @Autowired
    private IntrospectBizEndpoint         introspectBizEndpoint;

    public BizStateListener(ZkCommandClient zkCommandClient,
                            SofaDashboardProperties sofaDashboardProperties, Environment environment) {
        this.zkCommandClient = zkCommandClient;
        this.sofaDashboardProperties = sofaDashboardProperties;
        this.appName = environment.getProperty(Constants.APP_NAME_KEY);
    }

    /**
     * 只监听自己的 biz 节点
     * @throws Exception
     */
    public void start() {
        String ip = NetworkAddressUtils.getLocalIp(sofaDashboardProperties);
        String bizPath = Constants.SOFA_BOOT_CLIENT_ROOT + Constants.SOFA_BOOT_CLIENT_BIZ
                         + Constants.SEPARATOR + appName + Constants.SEPARATOR + ip;
        try {
            // 创建监听节点
            zkCommandClient.getCuratorClient().create().creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL).forPath(bizPath);
            // 监听 /apps/biz 下的节点
            NodeCache nodeCache = new NodeCache(zkCommandClient.getCuratorClient(), bizPath);
            addListener(nodeCache, bizPath);
        } catch (Throwable t) {
            LOGGER.error("Error to start listener to biz path {}", bizPath, t);
        }

    }

    private void addListener(final NodeCache cache,final String bizPath) throws Exception {
        System.out.println("当前节点：" + cache.getCurrentData());
        NodeCacheListener listener = ()-> {
            if (introspectBizEndpoint.bizState() instanceof ClientResponse){
                ClientResponse clientResponse = (ClientResponse) introspectBizEndpoint.bizState();
                byte[] bytes = ObjectBytesUtils.convertFromObject(clientResponse);
                zkCommandClient.getCuratorClient().setData().forPath(bizPath,bytes);
            }
        };
        System.out.println("变更之后：" + cache.getCurrentData());
        cache.getListenable().addListener(listener);
        cache.start();
    }
}
