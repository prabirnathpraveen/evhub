package com.evhub.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
// @EnableSwagger2
public class SwaggerConfig {

    // private ApiKey apiKey () {
    // return new ApiKey(SwaggerConstant.JWT.getValue(),
    // SwaggerConstant.AUTHORIZATION.getValue(), SwaggerConstant.HEADER.getValue());
    // }
    // @Bean
    // public Docket api() {
    // return new Docket(DocumentationType.SWAGGER_2)
    // .groupName("app-evhub-be")
    // .select()
    // .apis((Predicate<RequestHandler>)
    // RequestHandlerSelectors.basePackage("com.evhub.app.controller"))
    // .paths((Predicate<String>) PathSelectors.ant("/**"))
    // .build()
    // .apiInfo(apiInfo())
    // .useDefaultResponseMessages(false);
    // }

    // private ApiInfo apiInfo() {
    // return new ApiInfoBuilder().title("EV-HUB API Documentation")
    // .description("These are API's for EV-HUB")
    // .license("IOT83")
    // .version("1.0")
    // .build();
    // }

    @Bean
    public Docket postsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Java Techie")
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.regex("/api/v1/*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("Book Service")
                .description("Sample Documentation Generateed Using SWAGGER2 for our Book Rest API")
                .termsOfServiceUrl("https://www.youtube.com/channel/UCORuRdpN2QTCKnsuEaeK-kQ")
                .license("Java_Gyan_Mantra License")
                .licenseUrl("https://www.youtube.com/channel/UCORuRdpN2QTCKnsuEaeK-kQ").version("1.0").build();
    }
}
