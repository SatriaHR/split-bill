package com.splitbill.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.splitbill.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ControllerResponseTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private AppUserRepository users;

    @BeforeEach
    void cleanDatabase() {
        users.deleteAll();
    }

    @Test
    void signupReturnsStandardResponseEnvelope() throws Exception {
        MvcResult result = request("POST /api/auth/sign-up", "{\"username\":\"alice\",\"password\":\"password123\"}",
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User signed up successfully"))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andReturn();
        printResponse(result);
    }

    @Test
    void invalidSignupReturnsStandardErrorEnvelope() throws Exception {
        MvcResult result = request("POST /api/auth/sign-up", "{\"username\":\"\",\"password\":\"short\"}",
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.data.path").value("/api/auth/sign-up"))
                .andReturn();
        printResponse(result);
    }

    @Test
    void duplicateSignupReturnsDomainErrorEnvelope() throws Exception {
        String body = "{\"username\":\"alice\",\"password\":\"password123\"}";
        request("POST /api/auth/sign-up", body,
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        MvcResult result = request("POST /api/auth/sign-up", body,
                post("/api/auth/sign-up").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists"))
                .andExpect(jsonPath("$.data.path").value("/api/auth/sign-up"))
                .andReturn();
        printResponse(result);
    }

    private org.springframework.test.web.servlet.ResultActions request(String description, String body,
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        System.out.println("REQUEST " + description + " BODY " + body);
        return mockMvc.perform(request).andDo(MockMvcResultHandlers.print());
    }

    private void printResponse(MvcResult result) throws Exception {
        System.out.println("RESPONSE " + result.getResponse().getStatus() + " BODY " + result.getResponse().getContentAsString());
    }
}
