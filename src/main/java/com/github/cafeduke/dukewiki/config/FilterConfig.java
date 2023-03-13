package com.github.cafeduke.dukewiki.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.cafeduke.dukewiki.filter.RewriteFilter;

@Configuration
public class FilterConfig
{
    @Bean
    public FilterRegistrationBean<RewriteFilter> loggingFilter()
    {
        FilterRegistrationBean<RewriteFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new RewriteFilter());
        registrationBean.addUrlPatterns("/home/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
