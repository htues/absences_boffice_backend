package com.hftamayo.absencesbobe.features.companies.application.usecases;

import com.hftamayo.absencesbobe.features.companies.application.ports.in.CompanyCommandPort;
import com.hftamayo.absencesbobe.features.companies.application.ports.out.CompanyCommandRepositoryPort;
import com.hftamayo.absencesbobe.features.companies.domain.Company;
import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompanyCommandService implements CompanyCommandPort {

    private final CompanyCommandRepositoryPort companyRepository;

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> createCompany(Company company) {
        try {
            if (company == null) {
                return Result.error(ErrorApiResponse.BUSINESS_LOGIC_ERROR);
            }

            // Uniqueness ignores deleted rows (per repository policy)
            if (companyRepository.existsByName(company.getName())) {
                return Result.error(ErrorApiResponse.ENTITY_EXISTS);
            }

            Company saved = companyRepository.save(company);
            return Result.ok(saved);

        } catch (IllegalArgumentException ex) {
            return Result.error(ErrorApiResponse.VALIDATION_ERROR);

        } catch (DataIntegrityViolationException ex) {
            return Result.error(ErrorApiResponse.ENTITY_EXISTS);

        } catch (Exception ex) {
            return catchUnknownError("createCompany", company.getId(), ex);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> updateCompany(
            Long id,
            String name,
            String description,
            String address
    ) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorApiResponse.VALIDATION_ERROR);
            }

            // Normal reads ignore deleted rows -> deleted behaves like NOT_FOUND here
            return companyRepository.findById(id)
                    .map(existing -> {
                        String oldName = existing.getName();

                        existing.updateDetails(name, description, address);

                        boolean nameChanged = oldName == null
                                ? existing.getName() != null
                                : !oldName.equalsIgnoreCase(existing.getName());

                        // Uniqueness ignores deleted rows (per repository policy)
                        if (nameChanged && companyRepository.existsByNameExcludingId(existing.getName(), existing.getId())) {
                            return Result.<Company, ApiResponseDescriptor>error(ErrorApiResponse.ENTITY_EXISTS);
                        }

                        Company saved = companyRepository.save(existing);
                        return Result.<Company, ApiResponseDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorApiResponse.NOT_FOUND));

        } catch (IllegalArgumentException ex) {
            return Result.error(ErrorApiResponse.VALIDATION_ERROR);

        } catch (DataIntegrityViolationException ex) {
            return Result.error(ErrorApiResponse.ENTITY_EXISTS);

        } catch (Exception ex) {
            return catchUnknownError("updateCompany", id, ex);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> deleteCompany(Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorApiResponse.VALIDATION_ERROR);
            }

            // Delete is allowed to "see" deleted rows to stay idempotent
            return companyRepository.findByIdIncludingDeleted(id)
                    .map(existing -> {
                        if (existing.isDeleted()) {
                            return Result.<Company, ApiResponseDescriptor>error(ErrorApiResponse.BUSINESS_LOGIC_ERROR);
                        }

                        existing.markDeleted();
                        Company saved = companyRepository.save(existing);
                        return Result.<Company, ApiResponseDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorApiResponse.NOT_FOUND));
        } catch (Exception ex) {
            return catchUnknownError("deleteCompany", id, ex);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> restoreCompany(Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorApiResponse.VALIDATION_ERROR);
            }

            return companyRepository.findByIdIncludingDeleted(id)
                    .map(existing -> {
                        if (!existing.isDeleted()) {
                            return Result.<Company, ApiResponseDescriptor>ok(existing);
                        }

                        existing.restore();
                        Company saved = companyRepository.save(existing);
                        return Result.<Company, ApiResponseDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorApiResponse.NOT_FOUND));

        } catch (Exception ex) {
            return catchUnknownError("restoreCompany", id, ex);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> deactivateCompany(Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorApiResponse.VALIDATION_ERROR);
            }

            return companyRepository.findById(id)
                    .map(existing -> {
                        if (!existing.isActive()) {
                            return Result.<Company, ApiResponseDescriptor>ok(existing); // idempotent
                        }

                        existing.deactivate();
                        Company saved = companyRepository.save(existing);
                        return Result.<Company, ApiResponseDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorApiResponse.NOT_FOUND));

        } catch (Exception ex) {
            return catchUnknownError("deactivateCompany", id, ex);
        }
    }

    @Transactional
    @Override
    public Result<Company, ? extends ApiResponseDescriptor> activateCompany(Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error(ErrorApiResponse.VALIDATION_ERROR);
            }

            return companyRepository.findById(id)
                    .map(existing -> {
                        if (existing.isActive()) {
                            return Result.<Company, ApiResponseDescriptor>ok(existing); // idempotent
                        }

                        existing.activate();
                        Company saved = companyRepository.save(existing);
                        return Result.<Company, ApiResponseDescriptor>ok(saved);
                    })
                    .orElseGet(() -> Result.error(ErrorApiResponse.NOT_FOUND));

        } catch (Exception ex) {
            return catchUnknownError("activateCompany", id, ex);
        }
    }

    private <T> Result<T, ? extends ApiResponseDescriptor> catchUnknownError(String method, Long id, Exception ex) {
        log.error("method={} failed for id={}", method, id, ex);
        return Result.error(ErrorApiResponse.UNKNOWN_ERROR);
    }
}