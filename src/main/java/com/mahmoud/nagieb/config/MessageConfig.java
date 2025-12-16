package com.mahmoud.nagieb.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class MessageConfig {
    @Bean
    public MessageSource messageSource() {


        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        messageSource.setBasenames(
                "classpath:messages/customer/messages/messages",
                "classpath:messages/vendor/messages/messages",
                "classpath:messages/purchase/messages/messages",
                "classpath:messages/customer/ui/customer-list/customer-list",
                "classpath:messages/customer/ui/customer-details/customer-details",
                "classpath:messages/shared/validation/validation"
        );

        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

}
