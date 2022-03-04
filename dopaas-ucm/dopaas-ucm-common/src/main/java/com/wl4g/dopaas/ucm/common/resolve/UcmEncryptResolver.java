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
package com.wl4g.dopaas.ucm.common.resolve;

import java.io.Serializable;

import com.wl4g.dopaas.common.bean.ucm.model.ReleaseConfigInfo.ConfigSource;

/**
 * UCM property source interface definition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-08-15
 * @sine v1.0.0
 * @see
 */
public interface UcmEncryptResolver extends Serializable {

    /**
     * Resolve release source configuration cipher property.
     * 
     * @param source
     * @return
     */
    default ConfigSource resolve(ConfigSource source) {
        throw new UnsupportedOperationException();
    }

}