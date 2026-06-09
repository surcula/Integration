package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Company;
import be.atc.erpprojetintegration_1.interfaces.ICompanyService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CompanyBusiness {

    @Inject
    private ICompanyService companyService;

    /**
     * Retrieves the active company displayed in the administration page.
     *
     * @return active company result
     */
    public Result<Company> getActiveCompany() {
        return companyService.getActiveCompany();
    }

    /**
     * Validates and updates company information.
     *
     * @param company company to update
     * @return updated company result
     */
    public Result<Company> updateCompany(Company company) {
        if (company == null || company.getId() == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("company", "company.error.notFound");
            return Result.fail(errors);
        }

        Result<Void> validationResult = validateCompany(company);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        trimCompanyFields(company);
        return companyService.update(company);
    }

    private Result<Void> validateCompany(Company company) {
        Map<String, String> errors = new HashMap<>();

        FormValidator.required(company.getName(), "name", "company.error.name.required", errors);
        FormValidator.lengthBetween(company.getName(), "name", "company.error.name.length", 1, 200, errors);
        FormValidator.lengthBetween(company.getPhone(), "phone", "company.error.phone.length", 0, 20, errors);
        FormValidator.lengthBetween(company.getBank(), "bank", "company.error.bank.length", 0, 100, errors);
        FormValidator.lengthBetween(company.getIban(), "iban", "company.error.iban.length", 0, 34, errors);
        FormValidator.lengthBetween(company.getLogo(), "logo", "company.error.logo.length", 0, 500, errors);
        FormValidator.lengthBetween(company.getEmail(), "email", "company.error.email.length", 0, 150, errors);
        FormValidator.lengthBetween(company.getWebsite(), "website", "company.error.website.length", 0, 255, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimCompanyFields(Company company) {
        company.setName(trim(company.getName()));
        company.setPhone(trim(company.getPhone()));
        company.setBank(trim(company.getBank()));
        company.setIban(trim(company.getIban()));
        company.setLogo(trim(company.getLogo()));
        company.setEmail(trim(company.getEmail()));
        company.setWebsite(trim(company.getWebsite()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
