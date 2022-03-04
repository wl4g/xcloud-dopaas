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
package com.wl4g.dopaas.ucm.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.typesafe.config.parser.ConfigNode;
import com.wl4g.dopaas.common.bean.cmdb.AppCluster;
import com.wl4g.dopaas.common.bean.cmdb.AppInstance;
import com.wl4g.dopaas.common.bean.ucm.ConfigSourceBean;
import com.wl4g.dopaas.common.bean.ucm.ConfigVersion;
import com.wl4g.dopaas.common.bean.ucm.ConfigVersionList;
import com.wl4g.dopaas.common.bean.ucm.HistoryOfDetail;
import com.wl4g.dopaas.common.bean.ucm.ReleaseDetail;
import com.wl4g.dopaas.common.bean.ucm.Version;
import com.wl4g.dopaas.common.bean.ucm.VersionContentBean;
import com.wl4g.dopaas.common.bean.ucm.VersionOfDetail;
import com.wl4g.dopaas.common.bean.ucm.model.BaseConfigInfo.ConfigMeta;
import com.wl4g.dopaas.common.bean.ucm.model.ReleaseConfigInfoRequest;
import com.wl4g.dopaas.common.bean.ucm.model.ReportChangedRequest;
import com.wl4g.dopaas.ucm.common.exception.TooManyRefreshException;
import com.wl4g.dopaas.ucm.config.StandardUcmProperties;
import com.wl4g.dopaas.ucm.dao.ConfigurationDao;
import com.wl4g.dopaas.ucm.dao.HistoryDao;
import com.wl4g.dopaas.ucm.handler.CentralConfigServerHandler;
import com.wl4g.dopaas.ucm.service.ConfigurationService;

/**
 * DoPaaS configuration core service implements.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月6日
 * @since
 */
@Service
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    private @Autowired ConfigurationDao configurationDao;
    private @Autowired HistoryDao historyDao;
    private @Autowired AppClusterDao appClusterDao;
    private @Autowired AppInstanceDao appInstanceDao;
    private @Autowired CentralConfigServerHandler contextHandler;
    private @Autowired StandardUcmProperties ucmServerProperties;

    private static final Map<String, Long> refreshProtectIntervalMap = new HashMap<>();

    @Override
    public void configure(VersionOfDetail vd) {
        // 校验历史版本签名，签名相同的实例版本则排除后面相关操作
        List<String> nodeIdList = vd.getNodeIdList();
        if (nodeIdList == null || nodeIdList.isEmpty()) {// 如果实例id列表为空则不进行操作
            return;
        }
        checkRefreshProtectInterval(nodeIdList);
        updateRefreshTime(nodeIdList);
        String sign = signVersionContent(vd);
        List<AppInstance> nodeList = new ArrayList<>();
        for (String nodeId : nodeIdList) {
            AppInstance instance = this.appInstanceDao.selectByPrimaryKey(Integer.valueOf(nodeId));
            Integer hisVersionId = instance.getVersionId();
            if (hisVersionId != null) {
                Version version = new Version();
                version.setId(hisVersionId);
                version = this.historyDao.versionselect(version);
                String hisSign = version.getSign();
                if (sign.equals(hisSign)) {
                    continue;
                }
            }
            nodeList.add(instance);
        }
        if (nodeList.isEmpty()) {
            return;
        }
        // Save version information.
        vd.preInsert();
        vd.setSign(sign);
        vd.setSigntype(ConfigVersion.DEFUALT_SIGN);
        this.configurationDao.insert(vd);
        int versionId = vd.getId();

        // Save version details information.
        if (null != vd.getConfigGurations() && !vd.getConfigGurations().isEmpty()) {
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("vid", vd.getId());
            for (VersionContentBean versionContentBean : vd.getConfigGurations()) {
                versionContentBean.preInsert();
            }
            vMap.put("configGurations", vd.getConfigGurations());
            this.configurationDao.insertDetail(vMap);
        }

        // Save release history information.
        HistoryOfDetail historyOfDetail = new HistoryOfDetail();
        historyOfDetail.preInsert();
        historyOfDetail.setVersionid(versionId);
        historyOfDetail.setRemark(vd.getRemark());
        historyOfDetail.setCreateBy(vd.getCreateBy());
        historyOfDetail.setCreateDate(vd.getCreateDate());
        this.historyDao.insert(historyOfDetail);

        // Define release instance list.
        List<ConfigNode> instances = new ArrayList<>();
        for (AppInstance instance : nodeList) {
            // Save release history details information.
            ReleaseDetail releaseDetail = new ReleaseDetail();
            releaseDetail.preInsert();
            releaseDetail.setReleaseId(historyOfDetail.getId());
            releaseDetail.setResult("暂无结果");
            releaseDetail.setInstanceId(instance.getId());
            this.historyDao.insertDetail(releaseDetail);

            // Update instance/node reference version information.
            Map<String, Object> nMap = new HashMap<>();
            nMap.put("vid", vd.getId());
            nMap.put("nodeid", instance.getId());
            nMap.put("updateBy", vd.getUpdateBy());
            nMap.put("updateDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(vd.getUpdateDate()));
            this.configurationDao.updateNode(nMap);

            // Get application instance information.
            ConfigNode releaseInstance = new ConfigNode();
            // TODO
            releaseInstance.setHost(instance.getHostname());
            releaseInstance.setEndpoint(instance.getEndpoint());
            instances.add(releaseInstance);
        }
        // Get application group information.
        AppCluster appCluster = this.appClusterDao.selectByPrimaryKey(vd.getAppClusterId());

        List<String> namespaces = new ArrayList<>();
        for (VersionContentBean vcb : vd.getConfigGurations()) {
            namespaces.add(vcb.getNamespaceId());
        }

        // Request configuration source send to client.
        PreFetchCommand pre = new PreFetchCommand();
        pre.setCluster(appCluster.getName());
        pre.setProfiles(namespaces);
        ConfigMeta meta = new ConfigMeta(String.valueOf(historyOfDetail.getId()), String.valueOf(versionId));
        pre.setMeta(meta);
        pre.setNodes(instances);
        this.contextHandler.release(pre);
    }

    @Override
    public boolean update(ConfigVersion cof) {
        cof.preUpdate();
        return configurationDao.update(cof);
    }

    @Override
    public List<ConfigVersionList> list(ConfigVersionList agl) {
        return configurationDao.list(agl);
    }

    @Override
    public boolean deleteGuration(String id) {
        return configurationDao.deleteConfigGuration(id);
    }

    @Override
    public boolean updateGuration(VersionContentBean guration) {
        guration.preUpdate();
        return configurationDao.updateGuration(guration);
    }

    @Override
    public List<VersionContentBean> selectVersion(int id) {
        return configurationDao.selectVersion(id);
    }

    @Override
    public ConfigSourceBean findSource(ReleaseConfigInfoRequest release) {
        return this.configurationDao.findSource(release);
    }

    @Override
    public void updateReleaseDetail(ReportChangedRequest report) {
        this.configurationDao.updateReleaseDetail(report);
    }

    /**
     * Signature version file content.
     * 
     * @return
     */
    @SuppressWarnings("deprecation")
    private String signVersionContent(VersionOfDetail vd) {
        StringBuffer plain = new StringBuffer();
        if (vd.getConfigGurations() != null) {
            vd.getConfigGurations().forEach(c -> plain.append(c.getContent()));
            // Computational signature
            return Hashing.md5().newHasher(32).putString(plain, Charsets.UTF_8).hash().toString();
        }
        return null;
    }

    private void checkRefreshProtectInterval(List<String> nodeIdList) {
        for (String node : nodeIdList) {
            Long aLong = refreshProtectIntervalMap.get(node);
            if (Objects.nonNull(aLong)
                    && System.currentTimeMillis() - aLong <= ucmServerProperties.getRefreshProtectIntervalMs()) {
                throw new TooManyRefreshException("Too many refresh");
            }
        }
    }

    private void updateRefreshTime(List<String> nodeIdList) {
        long now = System.currentTimeMillis();
        for (String node : nodeIdList) {
            refreshProtectIntervalMap.put(node, now);
        }

    }

}