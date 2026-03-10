package com.hftamayo.absencesbobe.features.companies.adapters.web.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanyJpaEntity;
import com.hftamayo.absencesbobe.features.companies.adapters.persistence.CompanySpringDataRepository;
import com.hftamayo.absencesbobe.features.shared.test.AbstractPostgresIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        String requestBody = """
                {
                  "name": "Acme IT",
                  "description": "Technology company",
                  "address": "123 Main Street"
                }
                """;

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
        assertThat(data.path("name").asText()).isEqualTo("Acme IT");
        assertThat(data.path("description").asText()).isEqualTo("Technology company");
        assertThat(data.path("address").asText()).isEqualTo("123 Main Street");
        assertThat(data.path("active").asBoolean()).isTrue();
        assertThat(data.path("deleted").asBoolean()).isFalse();

        Long createdId = data.path("id").asLong();

        CompanyJpaEntity persisted = companyRepository.findById(createdId).orElseThrow();

        assertThat(persisted.getName()).isEqualTo("Acme IT");
        assertThat(persisted.getDescription()).isEqualTo("Technology company");
        assertThat(persisted.getAddress()).isEqualTo("123 Main Street");
        assertThat(persisted.isActive()).isTrue();
        assertThat(persisted.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} updates company and persists new values")
    void updateCompany_updatesAndPersistsChanges() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setName("Old Name");
        entity.setDescription("Old Description");
        entity.setAddress("Old Address");
        entity.setActive(true);
        entity.setDeleted(false);

        CompanyJpaEntity saved = companyRepository.saveAndFlush(entity);

        String requestBody = """
                {
                  "name": "New Name",
                  "description": "New Description",
                  "address": "New Address"
                }
                """;

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
        assertThat(data.path("name").asText()).isEqualTo("New Name");
        assertThat(data.path("description").asText()).isEqualTo("New Description");
        assertThat(data.path("address").asText()).isEqualTo("New Address");

        CompanyJpaEntity updated = companyRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getDescription()).isEqualTo("New Description");
        assertThat(updated.getAddress()).isEqualTo("New Address");
        assertThat(updated.isActive()).isTrue();
        assertThat(updated.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id}/deactivate deactivates company")
    void deactivateCompany_deactivatesCompany() throws Exception {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setName("Deactivate Me");
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
        entity.setName("Restore Me");
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
        entity.setName("Delete Me");
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
}