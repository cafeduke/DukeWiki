package com.github.cafeduke.dukewiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class DukeWikiApplication extends SpringBootServletInitializer
{
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) 
    {
        return builder.sources(DukeWikiApplication.class);
    }


    public static void main(String[] args)
    {
        SpringApplication.run(DukeWikiApplication.class, args);
    }
}
