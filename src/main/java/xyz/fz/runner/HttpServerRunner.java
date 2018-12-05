package xyz.fz.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.fz.http.server.HttpServer;

import javax.annotation.Resource;

@Component
public class HttpServerRunner implements CommandLineRunner {

    @Resource
    private HttpServer httpServer;

    @Override
    public void run(String... args) throws Exception {
        httpServer.start();
    }
}
