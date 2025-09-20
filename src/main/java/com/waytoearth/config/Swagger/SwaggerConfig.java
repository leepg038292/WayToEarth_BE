package com.waytoearth.config.Swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Way to Earth API")
                        .description("러닝 기반 Journey Running 및 크루 시스템 API")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Way to Earth Team")
                                .email("team@waytoearth.com")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. 전체 API")
                .pathsToMatch("/v1/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01. 인증 API")
                .pathsToMatch("/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("02. 사용자 API")
                .pathsToMatch("/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi runningApi() {
        return GroupedOpenApi.builder()
                .group("03. 러닝 기록 API")
                .pathsToMatch("/v1/running/**")
                .build();
    }

    @Bean
    public GroupedOpenApi feedApi() {
        return GroupedOpenApi.builder()
                .group("04. 피드 API")
                .pathsToMatch("/v1/feeds/**")
                .build();
    }

    @Bean
    public GroupedOpenApi statisticsApi() {
        return GroupedOpenApi.builder()
                .group("05. 통계 API")
                .pathsToMatch("/v1/statistics/**")
                .build();
    }

    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("06. 파일 업로드 API")
                .pathsToMatch("/v1/files/**")
                .build();
    }

    @Bean
    public GroupedOpenApi weatherApi() {
        return GroupedOpenApi.builder()
                .group("07. 날씨 API")
                .pathsToMatch("/v1/weather/**")
                .build();
    }

    @Bean
    public GroupedOpenApi emblemApi() {
        return GroupedOpenApi.builder()
                .group("08. 엠블럼 API")
                .pathsToMatch("/v1/emblems/**")
                .build();
    }



    @Bean
    public GroupedOpenApi themeCourseApi() {
        return GroupedOpenApi.builder()
                .group("09. 테마 코스 API")
                .pathsToMatch("/v1/theme-courses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi customCourseApi() {
        return GroupedOpenApi.builder()
                .group("10. 커스텀 코스 API")
                .pathsToMatch("/v1/custom-courses/**")
                .build();
    }

    @Bean
    public GroupedOpenApi courseSegmentApi() {
        return GroupedOpenApi.builder()
                .group("11. 코스 세그먼트 API")
                .pathsToMatch("/v1/course-segments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userVirtualCourseApi() {
        return GroupedOpenApi.builder()
                .group("12. 사용자 가상 코스 API")
                .pathsToMatch("/v1/user-virtual-courses/**")
                .build();
    }

    // ===== Journey Running APIs (New System) =====

    @Bean
    public GroupedOpenApi journeyApi() {
        return GroupedOpenApi.builder()
                .group("13. Journey 여정 관리 API")
                .pathsToMatch("/v1/journeys/**", "/v1/journey-progress/**")
                .build();
    }

    @Bean
    public GroupedOpenApi landmarkApi() {
        return GroupedOpenApi.builder()
                .group("14. Landmark 랜드마크 API")
                .pathsToMatch("/v1/landmarks/**", "/v1/story-cards/**")
                .build();
    }

    @Bean
    public GroupedOpenApi stampApi() {
        return GroupedOpenApi.builder()
                .group("15. Stamp 스탬프 수집 API")
                .pathsToMatch("/v1/stamps/**")
                .build();
    }

    @Bean
    public GroupedOpenApi guestbookApi() {
        return GroupedOpenApi.builder()
                .group("16. Guestbook 방명록 API")
                .pathsToMatch("/v1/guestbook/**")
                .build();
    }

}
