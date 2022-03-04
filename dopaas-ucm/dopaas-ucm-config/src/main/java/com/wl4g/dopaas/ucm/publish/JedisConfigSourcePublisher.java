/*
 * Copyright 2017 ~ 2050 the original author or authors <Wanglsir@gmail.com, 983708408@qq.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.dopaas.ucm.publish;

import com.wl4g.infra.support.cache.jedis.JedisService;
import com.wl4g.dopaas.ucm.config.UcmProperties;

import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.dopaas.ucm.common.UCMConstants.CACHE_PUB_GROUPS;
import static com.wl4g.dopaas.ucm.common.UCMConstants.KEY_PUB_PREFIX;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * UCM configuration source server publisher implements
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年5月27日
 * @since
 */
public class JedisConfigSourcePublisher extends GenericConfigSourcePublisher {

    final private JedisService jedisService;

    public JedisConfigSourcePublisher(UcmProperties config, JedisService jedisService) {
        super(config);
        this.jedisService = jedisService;
    }

    @Override
    protected Collection<PublishedConfigWrapper> pollNextPublishedConfig() {
        List<PublishedConfigWrapper> list = new ArrayList<>(4);

        // Extract published config.
        Set<Object> groups = jedisService.getObjectSet(CACHE_PUB_GROUPS);
        if (!isEmpty(groups)) {
            for (Object group : groups) {
                String key = getClusterKey((String) group);
                PublishedConfigWrapper wrap = jedisService.getObjectT(key, PublishedConfigWrapper.class);
                if (nonNull(wrap)) {
                    list.add(wrap);
                    // Release configured.
                    Long res = jedisService.del(key);
                    if (isNull(res) || res <= 0) {
                        log.warn("Failed to release published configuration, key: {}", key);
                    }
                }
            }
        }

        log.debug("Extract published config for - ({}), {}", list.size(), list);
        return list;
    }

    @Override
    protected void doPublishConfig(PublishedConfigWrapper published) {
        log.debug("Publishing config - {}", published);

        // Storage group name
        jedisService.setSetObjectAdd(CACHE_PUB_GROUPS, published.getCluster());

        // Storage group published
        jedisService.setObjectT(getClusterKey(published), published, 0);
    }

    private String getClusterKey(PublishedConfigWrapper published) {
        String key = Hex.encodeHexString(published.getZone().concat("_").concat(published.getCluster()).getBytes(UTF_8));
        return KEY_PUB_PREFIX.concat(key);
    }

}