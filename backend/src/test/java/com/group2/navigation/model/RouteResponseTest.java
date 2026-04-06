package com.group2.navigation.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RouteResponseTest {

    @Test
    void successfulResponse_hasCoordinatesAndStats() {
        List<double[]> coords = List.of(
                new double[]{43.65, -79.38},
                new double[]{43.66, -79.37}
        );

        RouteResponse response = new RouteResponse(coords, 1500.0, 18.0);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCoordinates()).hasSize(2);
        assertThat(response.getTotalDistance()).isEqualTo(1500.0);
        assertThat(response.getEstimatedTime()).isEqualTo(18.0);
        assertThat(response.getMessage()).isEqualTo("Route calculated successfully");
    }

    @Test
    void errorResponse_noCoordinates() {
        RouteResponse response = RouteResponse.error("No route found");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("No route found");
        assertThat(response.getCoordinates()).isNull();
    }

    @Test
    void defaultConstructor_notSuccessful() {
        RouteResponse response = new RouteResponse();
        assertThat(response.isSuccess()).isFalse();
    }

    @Test
    void setters_work() {
        RouteResponse response = new RouteResponse();
        response.setSuccess(true);
        response.setMessage("ok");
        response.setTotalDistance(500);
        response.setEstimatedTime(6.5);
        response.setCoordinates(List.of(new double[]{43.65, -79.38}));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getTotalDistance()).isEqualTo(500);
        assertThat(response.getEstimatedTime()).isEqualTo(6.5);
        assertThat(response.getCoordinates()).hasSize(1);
    }
}
