package com.hftamayo.absencesbobe.features.companies.application.usecases;

import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.CodeDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyCommandService implements CompanyCommandPort {

    private final CompanyRepositoryPort companyRepository;

    @Transactional
    @Override
    public Result<Company, ? extends CodeDescriptor> createCompany(Company company) {
        try {
            if (company == null) {
                return Result.error(ErrorCode.BUSINESS_LOGIC_ERROR);
            }

            // Optional: prevent duplicates at the application level (DB unique constraint is still the final guard)
            if (companyRepository.existsByName(company.getName())) {
                return Result.error(ErrorCode.ENTITY_EXISTS);
            }

            Company saved = companyRepository.save(company);
            return Result.ok(saved);

        } catch (IllegalArgumentException ex) {
            // Domain rejected blank/invalid input
            return Result.error(ErrorCode.VALIDATION_ERROR);

        } catch (DataIntegrityViolationException ex) {
            // Race condition: unique constraint or other integrity issue during save
            return Result.error(ErrorCode.ENTITY_EXISTS);

        } catch (Exception ex) {
            return Result.error(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends CodeDescriptor> updateCompany(
            Long id,
            String name,
            String description,
            String address
    ) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorCode.VALIDATION_ERROR);
            }

            return companyRepository.findById(id)
                    .map(existing -> {
                        if (existing.isDeleted()) {
                            return Result.<Company, CodeDescriptor>error(ErrorCode.BUSINESS_LOGIC_ERROR);
                        }

                        // Capture old name before mutation, so we can avoid a useless uniqueness query
                        String oldName = existing.getName();

                        // Domain mutation (keeps validation inside the aggregate)
                        existing.updateDetails(name, description, address);

                        // Only check uniqueness if the name actually changed
                        boolean nameChanged = oldName == null
                                ? existing.getName() != null
                                : !oldName.equalsIgnoreCase(existing.getName());

                        if (nameChanged && companyRepository.existsByNameExcludingId(existing.getName(), existing.getId())) {
                            return Result.<Company, CodeDescriptor>error(ErrorCode.ENTITY_EXISTS);
                        }

                        Company saved = companyRepository.save(existing);
                        return Result.<Company, CodeDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorCode.NOT_FOUND));

        } catch (IllegalArgumentException ex) {
            // Domain validation failed during updateDetails(...)
            return Result.error(ErrorCode.VALIDATION_ERROR);

        } catch (DataIntegrityViolationException ex) {
            return Result.error(ErrorCode.ENTITY_EXISTS);

        } catch (Exception ex) {
            return Result.error(ErrorCode.UNKNOWN_ERROR);
        }
    }

    @Transactional
    @Override
    public Result<Void, ? extends CodeDescriptor> deleteCompany(Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorCode.VALIDATION_ERROR);
            }

            return companyRepository.findById(id)
                    .map(existing -> {
                        if (existing.isDeleted()) {
                            // Idempotent delete
                            return Result.<Void, CodeDescriptor>ok(null);
                        }

                        existing.markDeleted(); // domain rule: deleted=true, active=false
                        companyRepository.save(existing);

                        return Result.<Void, CodeDescriptor>ok(null);
                    })
                    .orElseGet(() -> Result.error(ErrorCode.NOT_FOUND));

        } catch (Exception ex) {
            return Result.error(ErrorCode.UNKNOWN_ERROR);
        }
    }
}