package com.siemens.cto.aem.service.balancermanager.impl;

import com.siemens.cto.aem.common.domain.model.balancermanager.WorkerStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BalancerManagerHtmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalancerManagerHtmlParser.class);

    public String getUrlPath(final String host) {
        return "https://" + host + "/balancer-manager";
    }

    public String getWorkerUrlPath(final String host, final String balancerName, final String nonce, final String workerName) {
        return "https://" + host + "/balancer-manager" + "?b=" + balancerName + "&nonce=" + nonce + "&w=" + workerName;
    }

    public Map<String, String> findBalancers(final String content) {
        LOGGER.info("Entering findBalancers:");
        Map<String, String> balancers = new HashMap<>();
        final String foundStringPrefix = "<h3>LoadBalancer Status for <a href=\"/balancer-manager\\?b=";
        final String foundStringPost = "\\&nonce=";
        final String foundStringPostNonce = "\">balancer://";
        final String matchPattern = foundStringPrefix + ".*" + foundStringPost + ".*";
        String balancerName;
        String nonce;
        Pattern pattern = Pattern.compile(matchPattern);
        Matcher matcher = pattern.matcher(content);
        String matchString;
        while (matcher.find()) {
            matchString = matcher.group();
            balancerName = matchString.substring(matchString.indexOf(foundStringPrefix.replace("\\", "")) + foundStringPrefix.replace("\\", "").length(),
                    matchString.indexOf(foundStringPost.replace("\\", "")));
            nonce = matchString.substring(
                    matchString.indexOf(foundStringPost.replace("\\", "")) + foundStringPost.replace("\\", "").length(),
                    matchString.indexOf(foundStringPostNonce));
            balancers.put(balancerName, nonce);
        }
        return balancers;
    }

    public String getWorkerStatus(final String content, final WorkerStatusType type) {
        String ignoreError = "On";
        Pattern pattern = Pattern.compile(type.toString());
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            System.out.println(matcher.group());
            ignoreError = "Off";
        }
        return ignoreError;
    }

    public Map<String,String> findWorkerStatus(final String workerHtml) {
        Map<String, String> map = new HashMap<>();
        map.put(WorkerStatusType.IGNORE_ERRORS.name(), getWorkerStatus(workerHtml, WorkerStatusType.IGNORE_ERRORS));
        map.put(WorkerStatusType.DISABLED.name(), getWorkerStatus(workerHtml, WorkerStatusType.DISABLED));
        map.put(WorkerStatusType.DRAINING_MODE.name(), getWorkerStatus(workerHtml, WorkerStatusType.DRAINING_MODE));
        map.put(WorkerStatusType.HOT_STANDBY.name(), getWorkerStatus(workerHtml, WorkerStatusType.HOT_STANDBY));
        return map;
    }
}
