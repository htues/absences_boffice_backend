package com.hftamayo.absencesbobe.features.companies.adapters.web.query;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyQueryControllerIT extends AbstractPostgresIT {

    private static final String BASE_URL = "/api/v1/companies";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanySpringDataRepository companyRepository;

    @Test
    @DisplayName("GET /api/v1/companies returns only active and non-deleted companies")
    void getActiveCompanies_returnsOnlyActiveNonDeletedCompanies() throws Exception {
        companyRepository.deleteAll();

        saveCompany("Acme", "Tech", "Address 1", true, false);
        saveCompany("Globex", "Finance", "Address 2", true, false);
        saveCompany("Inactive Co", "Hidden", "Address 3", false, false);
        saveCompany("Deleted Co", "Hidden", "Address 4", true, true);

        String response = mockMvc.perform(get(BASE_URL).param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);
        assertThat(json.path("resultMessage").asText()).isNotBlank();

        JsonNode data = json.path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data).hasSize(2);

        Set<String> companyNames = extractNames(data);

        assertThat(companyNames).containsExactlyInAnyOrder("Acme", "Globex");
        assertThat(companyNames).doesNotContain("Inactive Co", "Deleted Co");

        JsonNode pagination = json.path("pagination");
        assertThat(pagination.isMissingNode()).isFalse();
        assertThat(pagination.path("pageIndex").asInt()).isEqualTo(0);
        assertThat(pagination.path("pageSize").asInt()).isEqualTo(20);
    }

    @Test
    @DisplayName("GET /api/v1/companies applies page and size parameters")
    void getActiveCompanies_appliesPaginationParameters() throws Exception {
        companyRepository.deleteAll();

        saveCompany("Acme", "Tech", "Address 1", true, false);
        saveCompany("Globex", "Finance", "Address 2", true, false);
        saveCompany("Initech", "Software", "Address 3", true, false);

        String response = mockMvc.perform(
                        get(BASE_URL)
                                .param("page", "0")
                                .param("size", "2")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertThat(json.path("responseType").asText()).isEqualTo("success");
        assertThat(json.path("statusCode").asInt()).isEqualTo(200);

        JsonNode data = json.path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.size()).isEqualTo(2);

        JsonNode pagination = json.path("pagination");
        assertThat(pagination.isMissingNode()).isFalse();
        assertThat(pagination.path("pageIndex").asInt()).isEqualTo(0);
        assertThat(pagination.path("pageSize").asInt()).isEqualTo(2);
    }

    private void saveCompany(
            String name,
            String description,
            String address,
            boolean active,
            boolean deleted
    ) {
        CompanyJpaEntity entity = new CompanyJpaEntity();
        entity.setName(name);
        entity.setDescription(description);
        entity.setAddress(address);
        entity.setActive(active);
        entity.setDeleted(deleted);
        companyRepository.saveAndFlush(entity);
    }

    private Set<String> extractNames(JsonNode data) {
        Set<String> names = new HashSet<>();
        for (JsonNode item : data) {
            names.add(item.path("name").asText());
        }
        return names;
    }
}