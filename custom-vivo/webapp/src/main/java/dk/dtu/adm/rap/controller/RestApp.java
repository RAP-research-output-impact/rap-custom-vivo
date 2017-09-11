package dk.dtu.adm.rap.controller;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/vds")
public class RestApp extends ResourceConfig {
    public RestApp() {
        packages("dk.dtu.adm.rap.controller.DataService");
    }
}