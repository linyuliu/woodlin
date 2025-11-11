package com.mumu.woodlin.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redisson 属性配置
 */
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    private SingleServerConfig singleServerConfig = new SingleServerConfig();

    public SingleServerConfig getSingleServerConfig() {
        return singleServerConfig;
    }

    public void setSingleServerConfig(SingleServerConfig singleServerConfig) {
        this.singleServerConfig = singleServerConfig;
    }

    public static class SingleServerConfig {
        private String address;
        private Integer database = 0;
        private String password;
        private Integer connectionPoolSize = 64;
        private Integer connectionMinimumIdleSize = 10;
        private Integer idleConnectionTimeout = 10000;
        private Integer connectTimeout = 10000;
        private Integer timeout = 3000;
        private Integer retryAttempts = 3;
        private Integer retryInterval = 1500;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getDatabase() {
            return database;
        }

        public void setDatabase(Integer database) {
            this.database = database;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(Integer connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        public Integer getConnectionMinimumIdleSize() {
            return connectionMinimumIdleSize;
        }

        public void setConnectionMinimumIdleSize(Integer connectionMinimumIdleSize) {
            this.connectionMinimumIdleSize = connectionMinimumIdleSize;
        }

        public Integer getIdleConnectionTimeout() {
            return idleConnectionTimeout;
        }

        public void setIdleConnectionTimeout(Integer idleConnectionTimeout) {
            this.idleConnectionTimeout = idleConnectionTimeout;
        }

        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(Integer retryAttempts) {
            this.retryAttempts = retryAttempts;
        }

        public Integer getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Integer retryInterval) {
            this.retryInterval = retryInterval;
        }
    }
}
