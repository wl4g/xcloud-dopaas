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
package com.wl4g.devops.erm.service;

import com.wl4g.components.core.web.model.PageModel;
import com.wl4g.devops.common.bean.erm.DockerRepository;
import com.wl4g.devops.common.bean.erm.model.RepositoryProject;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author vjay
 */
public interface DockerRepositoryService {

	PageModel<DockerRepository> page(PageModel<DockerRepository> pm, String name);

	List<DockerRepository> getForSelect();

	void save(DockerRepository dockerRepository);

	DockerRepository detail(Long id);

	void del(Long id);

	List<RepositoryProject> getRepositoryProjects(Long id, String address, String name)
			throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException;

}