package com.hftamayo.absencesbobe.shared.infrastructure.seed.tasks;

import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.infrastructure.seed.SeedTask;
import com.hftamayo.absencesbobe.features.seeding.adapters.persistence.SeedYamlLoader;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompaniesSeedTask implements SeedTask {

    private static final Logger log = LoggerFactory.getLogger(CompaniesSeedTask.class);

    private static final String ID = "companies.yaml";
    private static final String RESOURCE = "seed/companies.yaml";

    private final SeedYamlLoader loader;
    private final CompanyCommandPort companyCommandPort;

    public CompaniesSeedTask(SeedYamlLoader loader, CompanyCommandPort companyCommandPort) {
        this.loader = loader;
        this.companyCommandPort = companyCommandPort;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void run() {
        CompaniesSeedFile file = loader.loadFromClasspath(RESOURCE, CompaniesSeedFile.class);
        List<CompanySeedRow> companies = file.companies();

        if (companies == null || companies.isEmpty()) {
            log.info("No companies found in {}", RESOURCE);
            return;
        }

        for (CompanySeedRow row : companies) {
            seedOne(row);
        }
    }

    private void seedOne(CompanySeedRow row) {
        Company company = Company.createNew(row.name(), row.description(), row.address());
        Result<Company, ? extends ApiResponseDescriptor> result = companyCommandPort.createCompany(company);

        if (result == null) {
            throw new IllegalStateException("Seeding failed: createCompany returned null for " + row.name());
        }

        if (result.isSuccess()) {
            log.info("Seeded company: {}", row.name());
            return;
        }

        ApiResponseDescriptor err = result.error();
        if (err == ErrorApiResponse.ENTITY_EXISTS) {
            log.info("Company already exists, skipping: {}", row.name());
            return;
        }

        String msgKey = err == null ? "<null>" : err.getMessageKey();
        int status = err == null ? 0 : err.getStatusCode();
        throw new IllegalStateException("Failed to seed company '" + row.name() + "' (" + msgKey + ", " + status + ")");
    }

    public record CompaniesSeedFile(List<CompanySeedRow> companies) {}
    public record CompanySeedRow(String name, String description, String address) {}
}