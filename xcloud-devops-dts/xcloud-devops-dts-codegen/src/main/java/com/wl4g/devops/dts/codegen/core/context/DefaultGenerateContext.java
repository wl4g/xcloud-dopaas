/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.devops.dts.codegen.core.context;

import com.wl4g.devops.dts.codegen.bean.GenTable;
import com.wl4g.devops.dts.codegen.config.CodegenProperties;

import java.io.File;

import static com.wl4g.components.common.lang.Assert2.notNullOf;

/**
 * {@link DefaultGenerateContext}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-07
 * @since
 */
public class DefaultGenerateContext implements GenerateContext {

    private final CodegenProperties config;

    /**
     * {@link GenTable}
     */
    private final GenTable genTable;

    private final File jobDir;

    public DefaultGenerateContext(CodegenProperties config, GenTable genTable) {
        this.config = notNullOf(config, "config");
        this.genTable = notNullOf(genTable, "genTable");
        this.jobDir = config.getJobDir(genTable.getId());
    }

	@Override
	public CodegenProperties getConfig() {
		return config;
	}

	@Override
    public GenTable getGenTable() {
        return genTable;
    }

	@Override
	public File getJobDir() {
		return jobDir;
	}

}