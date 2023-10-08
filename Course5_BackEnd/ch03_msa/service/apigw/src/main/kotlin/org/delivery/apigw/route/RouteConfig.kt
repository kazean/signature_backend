package org.delivery.apigw.route

import org.delivery.apigw.filter.ServiceApiPrivateFilter
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RouteConfig(
    private val serviceApiPrivateFilter: ServiceApiPrivateFilter
) {
    @Bean
    fun gatewayRoutes(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route { spec ->
                spec.order(-1) // 우선순위
                spec.path(
                    "/service-api/api/**"
                ).filters { filterSpec ->
                    filterSpec.rewritePath("/service-api(?<segment>/?.*)", "\${segment}")
                    filterSpec.filter(serviceApiPrivateFilter.apply(ServiceApiPrivateFilter.Config())) // 필터 지정
                }.uri(
                    "http://localhost:8080" // 라우팅할 주소
                )
            }
            .build()
    }
}