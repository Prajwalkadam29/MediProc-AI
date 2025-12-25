package com.praj.mediprocess.config;

import com.vesoft.nebula.client.graph.NebulaPoolConfig;
import com.vesoft.nebula.client.graph.net.NebulaPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class NebulaConfig {

    @Bean(destroyMethod = "close")
    public NebulaPool nebulaPool() throws Exception {
        NebulaPool pool = new NebulaPool();
        NebulaPoolConfig config = new NebulaPoolConfig();
        config.setMaxConnSize(10);

        // USE "127.0.0.1" (localhost) because the app is running on Windows
        // The port 9669 is mapped from Docker to your machine.
        pool.init(Arrays.asList(new com.vesoft.nebula.client.graph.data.HostAddress("127.0.0.1", 9669)), config);
        return pool;
    }
}