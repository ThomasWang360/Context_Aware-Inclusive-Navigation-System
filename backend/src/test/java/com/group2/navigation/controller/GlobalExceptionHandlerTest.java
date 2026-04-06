package com.group2.navigation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mvc;

    @Test
    void methodArgumentNotValid_returnsCleanErrorJson() throws Exception {
        // Send invalid signup with missing required fields
        mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void constraintViolation_returnsCleanErrorJson() throws Exception {
        // Path variable constraint violation: userId must be >= 1
        mvc.perform(get("/api/auth/user/{id}", 0))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void typeMismatch_returnsCleanErrorJson() throws Exception {
        // String where a Long is expected
        mvc.perform(get("/api/auth/user/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void missingRequestParam_returnsCleanErrorJson() throws Exception {
        mvc.perform(get("/api/geocode"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.address").exists());
    }

    @Test
    void noStackTrace_inErrorResponse() throws Exception {
        String response = mvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"ab\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Should NOT contain stack trace indicators
        org.assertj.core.api.Assertions.assertThat(response)
            .doesNotContain("at com.group2")
            .doesNotContain("java.lang.")
            .doesNotContain("stackTrace");
    }
}
