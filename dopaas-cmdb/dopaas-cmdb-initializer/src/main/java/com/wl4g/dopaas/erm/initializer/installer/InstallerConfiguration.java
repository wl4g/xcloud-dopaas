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
package com.wl4g.dopaas.cmdb.initializer.installer;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import com.wl4g.dopaas.cmdb.initializer.installer.AbstractSoftInstaller.SoftVersion;

/**
 * {@link InstallerConfiguration}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
public class InstallerConfiguration {

	/** Current install versions */
	private final SoftVersion version;

	public InstallerConfiguration(SoftVersion version) {
		notNullOf(version, "version");
		this.version = version;
	}

	public SoftVersion getVersion() {
		return version;
	}

}