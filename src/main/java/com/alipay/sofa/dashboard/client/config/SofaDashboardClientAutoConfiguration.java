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
package com.alipay.sofa.dashboard.client.config;

import com.alipay.sofa.ark.springboot2.endpoint.IntrospectBizEndpoint;
import com.alipay.sofa.dashboard.client.listener.BizStateListener;
import com.alipay.sofa.dashboard.client.listener.SofaDashboardClientApplicationContextClosedListener;
import com.alipay.sofa.dashboard.client.listener.SofaDashboardClientApplicationContextRefreshedListener;
import com.alipay.sofa.dashboard.client.registration.SofaDashboardClientRegister;
import com.alipay.sofa.dashboard.client.zookeeper.ZkCommandClient;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/2/15 2:03 PM
 * @since:
 **/
@Configuration
@EnableConfigurationProperties({ SofaDashboardProperties.class })
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "com.alipay.sofa.dashboard.client", value = "enable", matchIfMissing = true)
@ConditionalOnClass(CuratorFramework.class)
public class SofaDashboardClientAutoConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    @ConditionalOnMissingBean
    public SofaDashboardClientRegister sofaDashboardClientRegister(SofaDashboardProperties sofaClientProperties,
                                                                   ZkCommandClient commandClient) {
        if (StringUtils.isEmpty(sofaClientProperties.getZookeeper().getAddress())) {
            throw new RuntimeException("please config dashboard client zookeeper address.");
        }
        return new SofaDashboardClientRegister(sofaClientProperties, commandClient, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public ZkCommandClient zkCommandClient() {
        return new ZkCommandClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public BizStateListener bizStateListener(SofaDashboardProperties sofaClientProperties,
                                             ZkCommandClient commandClient) {
        return new BizStateListener(commandClient, sofaClientProperties, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    public SofaDashboardClientApplicationContextRefreshedListener sofaDashboardClientApplicationContextRefreshedListener() {
        return new SofaDashboardClientApplicationContextRefreshedListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public SofaDashboardClientApplicationContextClosedListener sofaDashboardClientApplicationContextClosedListener() {
        return new SofaDashboardClientApplicationContextClosedListener();
    }
}
