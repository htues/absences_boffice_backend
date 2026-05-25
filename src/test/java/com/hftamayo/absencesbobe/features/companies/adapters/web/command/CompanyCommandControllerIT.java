package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanyJpaEntity;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanySpringDataRepository;
import com.hftamayo.absencesbobe.shared.test.AbstractPostgresIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyCommandControllerIT extends AbstractPostgresIT {

    private static final String BASE_URL = "/api/v1/companies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanySpringDataRepository companyRepository;

    @Test
    @DisplayName("POST /api/v1/companies creates company and persists it")
    void createCompany_createsAndPersistsCompany() throws Exception {
        String targetName = "Acme-" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = """
        {
          "name": "%s",
          "description": "Technology company",
          "address": "123 Main Street"
        }
        """.formatted(targetName);

        String response = mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(201);
        assertThat(json.path("resultMessage").asText()).isNotBlank();

        JsonNode data = json.path("data");
        assertThat(data.isMissingNode()).isFalse();
        assertThat(data.path("id").asLong()).isPositive();
        assertThat(data.path("name").asText()).startsWith("Acme");
        assertThat(data.path("description").asText()).isEqualTo("Technology company");
        assertThat(data.path("address").asText()).isEqualTo("123 Main Street");
        assertThat(data.path("active").asBoolean()).isTrue();
        assertThat(data.path("deleted").asBoolean()).isFalse();

        Long createdId = data.path("id").asLong();

        CompanyJpaEntity persisted = companyRepository.findById(createdId).orElseThrow();

        assertThat(persisted.getName()).startsWith("Acme");
        assertThat(persisted.getDescription()).isEqualTo("Technology company");
        assertThat(persisted.getAddress()).isEqualTo("123 Main Street");
        assertThat(persisted.isActive()).isTrue();
        assertThat(persisted.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} updates company and persists new values")
    void updateCompany_updatesAndPersistsChanges() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        String targetName = "OldAcme-" + UUID.randomUUID().toString().substring(0, 8);
        entity.setName(targetName);
        entity.setDescription("Old Description");
        entity.setAddress("Old Address");
        entity.setActive(true);
        entity.setDeleted(false);

        CompanyJpaEntity saved = companyRepository.saveAndFlush(entity);

        targetName = "NewAcme-" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = """
                {
                  "name": "%s",
                  "description": "New Description",
                  "address": "New Address"
                }
                """.formatted(targetName);

        String response = mockMvc.perform(
                        put(BASE_URL + "/{id}", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);

        JsonNode data = json.path("data");
        assertThat(data.path("id").asLong()).isEqualTo(saved.getId());
        assertThat(data.path("name").asText()).startsWith("NewAcme");
        assertThat(data.path("description").asText()).isEqualTo("New Description");
        assertThat(data.path("address").asText()).isEqualTo("New Address");

        CompanyJpaEntity updated = companyRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getName()).startsWith("NewAcme");
        assertThat(updated.getDescription()).isEqualTo("New Description");
        assertThat(updated.getAddress()).isEqualTo("New Address");
        assertThat(updated.isActive()).isTrue();
        assertThat(updated.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id}/deactivate deactivates company")
    void deactivateCompany_deactivatesCompany() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        String targetName = "DeactivateMe-" + UUID.randomUUID().toString().substring(0, 8);
        entity.setName(targetName);
        entity.setDescription("Company to deactivate");
        entity.setAddress("456 Sunset Blvd");
        entity.setActive(true);
        entity.setDeleted(false);

        CompanyJpaEntity saved = companyRepository.saveAndFlush(entity);

        String response = mockMvc.perform(
                        put(BASE_URL + "/{id}/deactivate", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);
        assertThat(json.path("data").path("id").asLong()).isEqualTo(saved.getId());
        assertThat(json.path("data").path("active").asBoolean()).isFalse();

        CompanyJpaEntity updated = companyRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id}/restore restores a deleted company")
    void restoreCompany_restoresDeletedCompany() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        String targetName = "RestoreMe-" + UUID.randomUUID().toString().substring(0, 8);
        entity.setName(targetName);
        entity.setDescription("Deleted company");
        entity.setAddress("789 Oak Avenue");
        entity.setActive(false);
        entity.setDeleted(true);

        CompanyJpaEntity saved = companyRepository.saveAndFlush(entity);

        String response = mockMvc.perform(
                        put(BASE_URL + "/{id}/restore", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);
        assertThat(json.path("data").path("id").asLong()).isEqualTo(saved.getId());
        assertThat(json.path("data").path("active").asBoolean()).isTrue();
        assertThat(json.path("data").path("deleted").asBoolean()).isFalse();

        CompanyJpaEntity restored = companyRepository.findById(saved.getId()).orElseThrow();
        assertThat(restored.isActive()).isTrue();
        assertThat(restored.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{id} soft deletes company")
    void deleteCompany_softDeletesCompany() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        String targetName = "DeleteMe-" + UUID.randomUUID().toString().substring(0, 8);
        entity.setName("DeleteMe");
        entity.setDescription("Company to delete");
        entity.setAddress("321 Pine Road");
        entity.setActive(true);
        entity.setDeleted(false);

        CompanyJpaEntity saved = companyRepository.saveAndFlush(entity);

        String response = mockMvc.perform(
                        delete(BASE_URL + "/{id}", saved.getId())
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);

        CompanyJpaEntity deleted = companyRepository.findById(saved.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.isActive()).isFalse();
    }

    @Test
    @DisplayName("POST /api/v1/companies exceeds rate limit returns 429")
    void createCompany_exceedsRateLimit_returns429() throws Exception {
        String requestBody = """
        {
          "name": "RateLimitTest",
          "description": "Technology company",
          "address": "123 Main Street"
        }
        """;

        // The limit is 5 tokens. We perform 6 requests.
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
            ).andExpect(status().isCreated());
        }

        mockMvc.perform(
                post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isTooManyRequests());
    }
}