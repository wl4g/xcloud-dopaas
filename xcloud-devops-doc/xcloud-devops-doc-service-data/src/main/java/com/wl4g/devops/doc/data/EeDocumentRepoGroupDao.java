// Generated by XCloud DevOps for Codegen, refer: http://dts.devops.wl4g.com

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

package com.wl4g.devops.doc.data;

import com.wl4g.devops.common.bean.doc.EeDocumentRepoGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * {@link EeDocumentRepoGroup}
 *
 * @author root
 * @version 0.0.1-SNAPSHOT
 * @Date Feb 3, 2021
 * @since v1.0
 */
public interface EeDocumentRepoGroupDao {

    int insertSelective(EeDocumentRepoGroup eeDocumentRepoGroup);

    int deleteByPrimaryKey(Long id);

    EeDocumentRepoGroup selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(EeDocumentRepoGroup eeDocumentRepoGroup);

    int updateByPrimaryKey(EeDocumentRepoGroup eeDocumentRepoGroup);

    List<EeDocumentRepoGroup> list(@Param("eeDocumentRepoGroup") EeDocumentRepoGroup eeDocumentRepoGroup);

}