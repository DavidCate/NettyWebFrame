package com.aimango.robot.server.core.constant;

public class ServerConfig {
    public static final String SERVER_PORT="server.port";

    public static final String SERVER_NAME = "server.name";

    public static final String NETWORK_CARD="network.card";

    public class Config{
        public static final String CONFIG_MODE_LOCAL="local";
        public static final String CONFIG_MODE_NACOS="nacos";
        public static final String CONFIG_MODE = "server.config.mode";
    }

    public class Nacos {
        public static final String SERVER_ADDR="nacos.server.addr";
        public static final String NAMESPACE="nacos.config.namespace";

        public class CONFIG{
            public static final String FILE_EXTENSION="nacos.config.file-extension";
            public static final String GROUP="nacos.config.group";
        }

        public class DISCOVERY{
            public static final String SERVICE_NAME="nacos.discovery.service";
            public static final String GROUP="nacos.discovery.group";
            public static final String CLUSTER_NAME="nacos.discovery.cluster-name";
            public static final String WEIGHT="nacos.discovery.weight";
        }
    }

    public class Druid{
        public static final String URL="druid.url";
        public static final String USERNAME="druid.username";
        public static final String PASSWORD="druid.password";
        public static final String CONNECTION_INIT_SQLS="druid.connection-init-sqls";
        public static final String INITIALSIZE="druid.initialSize";
        public static final String MINIDLE="druid.minIdle";
        public static final String MAXACTIVE="druid.maxActive";
        public static final String MAXWAIT="druid.maxWait";
    }

    public class Mybatis{
        public static final String CONFIG_FILE_LOCATION="mybatis.config.location";
    }
}
