/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.utils;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.selector.SelectorType;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.naming.core.Instance;
import com.alibaba.nacos.naming.core.Service;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service util.
 *
 * @author xiweng.yy
 */
public class ServiceUtil {
    
    /**
     * Select service name with group name.
     *
     * @param services  service map
     * @param groupName group name
     * @return service names with group name
     */
    public static Map<String, Service> selectServiceWithGroupName(Map<String, Service> services, String groupName) {
        if (null == services || services.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<String, Service> result = new HashMap<>(services.size());
        String groupKey = groupName + Constants.SERVICE_INFO_SPLITER;
        for (Map.Entry<String, Service> each : services.entrySet()) {
            if (each.getKey().startsWith(groupKey)) {
                result.put(each.getKey(), each.getValue());
            }
        }
        return result;
    }
    
    /**
     * Select service name by selector.
     *
     * @param serviceMap     service name list
     * @param selectorString selector serialize string
     * @return service names filter by group name
     */
    public static Map<String, Service> selectServiceBySelector(Map<String, Service> serviceMap, String selectorString) {
        Map<String, Service> result = serviceMap;
        if (StringUtils.isNotBlank(selectorString)) {
            
            JsonNode selectorJson = JacksonUtils.toObj(selectorString);
            
            SelectorType selectorType = SelectorType.valueOf(selectorJson.get("type").asText());
            String expression = selectorJson.get("expression").asText();
            
            if (SelectorType.label.equals(selectorType) && StringUtils.isNotBlank(expression)) {
                expression = StringUtils.deleteWhitespace(expression);
                // Now we only support the following expression:
                // INSTANCE.metadata.xxx = 'yyy' or
                // SERVICE.metadata.xxx = 'yyy'
                String[] terms = expression.split("=");
                String[] factors = terms[0].split("\\.");
                switch (factors[0]) {
                    case "INSTANCE":
                        result = filterInstanceMetadata(serviceMap, factors[factors.length - 1],
                                terms[1].replace("'", ""));
                        break;
                    case "SERVICE":
                        result = filterServiceMetadata(serviceMap, factors[factors.length - 1],
                                terms[1].replace("'", ""));
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }
    
    private static Map<String, Service> filterInstanceMetadata(Map<String, Service> serviceMap, String key,
            String value) {
        Map<String, Service> result = new HashMap<>(serviceMap.size());
        for (Map.Entry<String, Service> each : serviceMap.entrySet()) {
            for (Instance address : each.getValue().allIPs()) {
                if (address.getMetadata() != null && value.equals(address.getMetadata().get(key))) {
                    result.put(each.getKey(), each.getValue());
                    break;
                }
            }
        }
        return result;
    }
    
    private static Map<String, Service> filterServiceMetadata(Map<String, Service> serviceMap, String key,
            String value) {
        Map<String, Service> result = new HashMap<>(serviceMap.size());
        for (Map.Entry<String, Service> each : serviceMap.entrySet()) {
            if (value.equals(each.getValue().getMetadata().get(key))) {
                result.put(each.getKey(), each.getValue());
            }
        }
        return result;
    }
    
    /**
     * Page service name.
     *
     * @param pageNo     page number
     * @param pageSize   size per page
     * @param serviceMap service source map
     * @return service name list by paged
     */
    public static List<String> pageServiceName(int pageNo, int pageSize, Map<String, Service> serviceMap) {
        return pageServiceName(pageNo, pageSize, serviceMap.keySet());
    }
    
    /**
     * Page service name.
     *
     * @param pageNo         page number
     * @param pageSize       size per page
     * @param serviceNameSet service name set
     * @return service name list by paged
     */
    public static List<String> pageServiceName(int pageNo, int pageSize, Collection<String> serviceNameSet) {
        List<String> result = new ArrayList<>(serviceNameSet);
        int start = (pageNo - 1) * pageSize;
        if (start < 0) {
            start = 0;
        }
        int end = start + pageSize;
        if (end > result.size()) {
            end = result.size();
        }
        for (int i = start; i < end; i++) {
            String serviceName = result.get(i);
            int indexOfSplitter = serviceName.indexOf(Constants.SERVICE_INFO_SPLITER);
            if (indexOfSplitter > 0) {
                serviceName = serviceName.substring(indexOfSplitter + 2);
            }
            result.set(i, serviceName);
        }
        return result.subList(start, end);
    }
}