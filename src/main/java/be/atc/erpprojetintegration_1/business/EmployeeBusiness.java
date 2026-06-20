package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.dto.EmployeeDetailsDto;
import be.atc.erpprojetintegration_1.dto.AddressEditDto;
import be.atc.erpprojetintegration_1.dto.EmployeeEditDto;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IAddressService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.mappers.EmployeeMapper;
import be.atc.erpprojetintegration_1.tools.Result;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeBusiness {

    private static final String TEMPORARY_PASSWORD = "ChangeMe123!";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d).{8,255}$";
    private static final String TEMPORARY_UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String TEMPORARY_LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
    private static final String TEMPORARY_DIGITS = "23456789";
    private static final String TEMPORARY_SYMBOLS = "!@#$%";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Inject
    private IEmployeeService employeeService;

    @Inject
    private IDepartmentHeadService departmentHeadService;
    @Inject
    private IEmployeeDepartmentService employeeDepartment;
    @Inject
    private IAddressService addressService;
    @Inject
    private ICitiesService citiesService;


    /**
     * Checks a plain password against a BCrypt hashed password.
     *
     * @param password plain password
     * @param hashedPassword hashed password
     * @return true if the password matches
     */
    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * Hashes a password with BCrypt.
     *
     * @param password plain password
     * @return hashed password
     */
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Generates a random temporary password that satisfies the password policy.
     *
     * @return temporary plain password
     */
    private String generateTemporaryPassword() {
        List<Character> characters = new ArrayList<>();
        characters.add(randomCharacter(TEMPORARY_UPPERCASE));
        characters.add(randomCharacter(TEMPORARY_LOWERCASE));
        characters.add(randomCharacter(TEMPORARY_DIGITS));
        characters.add(randomCharacter(TEMPORARY_SYMBOLS));

        String allCharacters = TEMPORARY_UPPERCASE + TEMPORARY_LOWERCASE
                + TEMPORARY_DIGITS + TEMPORARY_SYMBOLS;
        while (characters.size() < 12) {
            characters.add(randomCharacter(allCharacters));
        }

        Collections.shuffle(characters, SECURE_RANDOM);
        StringBuilder password = new StringBuilder(characters.size());
        for (Character character : characters) {
            password.append(character);
        }
        return password.toString();
    }

    private char randomCharacter(String characters) {
        return characters.charAt(SECURE_RANDOM.nextInt(characters.length()));
    }


    /**
     * Validates the login form before sending credentials to Shiro.
     *
     * @param email employee email
     * @param password employee password
     * @return validation result
     */
    public Result<Void> validateLoginForm(String email, String password) {
        Map<String, String> errors = new HashMap<>();

        FormValidator.required(email, "email", "login.email.required", errors);
        FormValidator.required(password, "password", "login.password.required", errors);
        FormValidator.lengthBetween(email, "email", "login.email.length", 5, 150, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    /**
     * Validates and replaces the temporary password of an employee.
     *
     * @param employeeId connected employee id
     * @param currentPassword current plain password, required for a voluntary change
     * @param currentPasswordRequired whether the current password must be checked
     * @param newPassword new plain password
     * @param passwordConfirmation repeated new password
     * @return password update result
     */
    public Result<Void> changePassword(Integer employeeId, String currentPassword,
                                       boolean currentPasswordRequired, String newPassword,
                                       String passwordConfirmation) {
        Map<String, String> errors = new HashMap<>();

        if (employeeId == null) {
            errors.put("employeeId", "password.change.error.employee.required");
        }

        FormValidator.required(newPassword, "newPassword",
                "password.change.error.required", errors);
        FormValidator.required(passwordConfirmation, "passwordConfirmation",
                "password.change.error.confirmation.required", errors);
        if (currentPasswordRequired) {
            FormValidator.required(currentPassword, "currentPassword",
                    "password.change.error.current.required", errors);
        }

        if (newPassword != null && !newPassword.isEmpty()
                && !newPassword.matches(PASSWORD_PATTERN)) {
            errors.put("newPassword", "password.change.error.format");
        }

        if (newPassword != null && !newPassword.isEmpty()
                && passwordConfirmation != null && !passwordConfirmation.isEmpty()
                && !newPassword.equals(passwordConfirmation)) {
            errors.put("passwordConfirmation", "password.change.error.mismatch");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        Result<Employee> employeeResult = employeeService.getById(employeeId);
        if (!employeeResult.isSuccess()) {
            return Result.fail(employeeResult.getErrors());
        }

        Employee employee = employeeResult.getData();

        if (currentPasswordRequired
                && !checkPassword(currentPassword, employee.getPassword())) {
            errors.put("currentPassword", "password.change.error.current.invalid");
            return Result.fail(errors);
        }

        if (checkPassword(newPassword, employee.getPassword())) {
            errors.put("newPassword", "password.change.error.same");
            return Result.fail(errors);
        }

        employee.setPassword(hashPassword(newPassword));
        employee.setMustChangePassword(false);

        Result<Employee> updateResult = employeeService.update(employee);
        return updateResult.isSuccess()
                ? Result.ok()
                : Result.fail(updateResult.getErrors());
    }

    /**
     * Generates and stores a temporary password for an active employee.
     *
     * @param employeeId employee identifier
     * @return temporary plain password, displayed once to the administrator
     */
    public Result<String> resetPassword(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employee.resetPassword.error.id.required");
            return Result.fail(errors);
        }

        String temporaryPassword = generateTemporaryPassword();
        Result<Void> resetResult = employeeService.resetPassword(
                employeeId, hashPassword(temporaryPassword));

        return resetResult.isSuccess()
                ? Result.ok(temporaryPassword)
                : Result.fail(resetResult.getErrors());
    }

    /**
     * Retrieves the connected employee profile with department information.
     *
     * @param employeeId employee id
     * @return profile result
     */
    public Result<EmployeeProfileDto> getProfile(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "profile.error.employee.required");
            return Result.fail(errors);
        }

        Result<EmployeeDepartment> employeDepartementResult =
                employeeDepartment.getActiveEmployeeDepartmentByEmployeeId(employeeId);


        if (!employeDepartementResult.isSuccess()) {
            return Result.fail(employeDepartementResult.getErrors());
        }
        EmployeeProfileDto profileDto = EmployeeMapper.toEmployeeProfileDto(employeDepartementResult.getData());

        return Result.ok(profileDto);
    }

    /**
     * Retrieves active employees for the list page.
     *
     * @return employee list dto result
     */
    public Result<List<EmployeeListDto>> getEmployeeList() {
        return getEmployeeList(false);
    }

    /**
     * Retrieves employees for the list page and optionally includes inactive employees.
     *
     * @param includeInactive true to include inactive employees
     * @return employee list dto result
     */
    public Result<List<EmployeeListDto>> getEmployeeList(boolean includeInactive) {
        Result<List<Employee>> result = includeInactive
                ? employeeService.getAllWithDepartments()
                : employeeService.getAllActiveWithDepartments();

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        List<EmployeeListDto> employees = result.getData()
                .stream()
                .map(EmployeeMapper::toEmployeeListDto)
                .collect(Collectors.toList());

        return Result.ok(employees);
    }

    public Result<List<EmployeeListDto>> getEmployeeListForViewer(
            Integer viewerEmployeeId, boolean globalAccess, boolean includeInactive) {
        Result<List<EmployeeListDto>> employeeResult = getEmployeeList(globalAccess && includeInactive);
        if (!employeeResult.isSuccess() || globalAccess) return employeeResult;

        Result<List<DepartmentHead>> headResult = departmentHeadService
                .getActiveByEmployeeId(viewerEmployeeId);
        if (!headResult.isSuccess()) return Result.fail(headResult.getErrors());

        List<String> managedDepartmentNames = headResult.getData().stream()
                .map(head -> head.getDepartment().getDepartmentName())
                .collect(Collectors.toList());
        return Result.ok(employeeResult.getData().stream()
                .filter(employee -> managedDepartmentNames.contains(employee.getDepartmentName()))
                .collect(Collectors.toList()));
    }

    public Result<Boolean> isDepartmentHead(Integer employeeId) {
        if (employeeId == null) return Result.ok(false);
        Result<List<DepartmentHead>> result = departmentHeadService.getActiveByEmployeeId(employeeId);
        return result.isSuccess() ? Result.ok(!result.getData().isEmpty()) : Result.fail(result.getErrors());
    }

    /**
     * Retrieves active cities for employee address forms.
     *
     * @return active city list result
     */
    public Result<List<City>> getActiveCities() {
        return citiesService.getAllActiveCities();
    }

    /**
     * Retrieves an employee and maps it for the edit form.
     *
     * @param employeeId employee id
     * @return employee edit dto result
     */
    public Result<EmployeeEditDto> getEmployeeForEdit(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employee.edit.error.id.required");
            return Result.fail(errors);
        }

        Result<Employee> employeeResult = employeeService.getById(employeeId);

        if (!employeeResult.isSuccess()) {
            return Result.fail(employeeResult.getErrors());
        }

        return Result.ok(EmployeeMapper.toEmployeeEditDto(employeeResult.getData()));
    }

    /**
     * Retrieves and maps the read-only details of an employee.
     *
     * @param employeeId employee identifier
     * @return employee details result
     */
    public Result<EmployeeDetailsDto> getEmployeeDetails(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employees.details.error.id.required");
            return Result.fail(errors);
        }

        Result<Employee> result = employeeService.getDetailsById(employeeId);
        return result.isSuccess()
                ? Result.ok(EmployeeMapper.toEmployeeDetailsDto(result.getData()))
                : Result.fail(result.getErrors());
    }

    /**
     * Validates and updates basic employee information.
     *
     * @param dto employee edit dto
     * @return updated employee edit dto result
     */
    public Result<EmployeeEditDto> updateBasicInformation(EmployeeEditDto dto) {
        Result<Void> validationResult = validateBasicInformationForm(dto, true);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<Employee> employeeResult = employeeService.getById(dto.getId());

        if (!employeeResult.isSuccess()) {
            return Result.fail(employeeResult.getErrors());
        }

        Employee employee = employeeResult.getData();
        EmployeeMapper.updateEmployeeFromEditDto(dto, employee);

        Result<Employee> updateResult = employeeService.update(employee);

        if (!updateResult.isSuccess()) {
            return Result.fail(updateResult.getErrors());
        }

        return Result.ok(EmployeeMapper.toEmployeeEditDto(updateResult.getData()));
    }

    /**
     * Validates and creates an employee from the basic information form.
     *
     * @param dto employee edit dto
     * @return created employee edit dto result
     */
    public Result<EmployeeEditDto> createBasicInformation(EmployeeEditDto dto) {
        Result<Void> validationResult = validateBasicInformationForm(dto, false);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Employee employee = EmployeeMapper.toEmployee(dto);
        employee.setPassword(hashPassword(TEMPORARY_PASSWORD));
        employee.setMustChangePassword(true);

        Result<Employee> createResult = employeeService.create(employee);

        if (!createResult.isSuccess()) {
            return Result.fail(createResult.getErrors());
        }

        return Result.ok(EmployeeMapper.toEmployeeEditDto(createResult.getData()));
    }

    /**
     * Validates, creates or updates the address attached to an employee.
     *
     * @param employeeId employee id
     * @param dto address edit dto
     * @return updated employee edit dto result
     */
    public Result<EmployeeEditDto> updateAddress(Integer employeeId, AddressEditDto dto) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employee.edit.error.id.required");
            return Result.fail(errors);
        }

        Result<Void> validationResult = validateAddressForm(dto);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<Employee> employeeResult = employeeService.getById(employeeId);

        if (!employeeResult.isSuccess()) {
            return Result.fail(employeeResult.getErrors());
        }

        Result<City> cityResult = citiesService.getById(dto.getCityId());

        if (!cityResult.isSuccess()) {
            return Result.fail(cityResult.getErrors());
        }

        Employee employee = employeeResult.getData();
        Address address = employee.getAddress();

        if (address == null) {
            address = new Address();
        }

        EmployeeMapper.updateAddressFromEditDto(dto, address, cityResult.getData());

        Result<Address> addressResult = address.getId() == null
                ? addressService.create(address)
                : addressService.update(address);

        if (!addressResult.isSuccess()) {
            return Result.fail(addressResult.getErrors());
        }

        employee.setAddress(addressResult.getData());
        Result<Employee> updateEmployeeResult = employeeService.update(employee);

        if (!updateEmployeeResult.isSuccess()) {
            return Result.fail(updateEmployeeResult.getErrors());
        }

        return Result.ok(EmployeeMapper.toEmployeeEditDto(updateEmployeeResult.getData()));
    }

    /**
     * Soft deletes an employee by setting them inactive.
     *
     * @param employeeId employee id
     * @return result
     */
    public Result<Void> deactivateEmployee(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employee.delete.error.id.required");
            return Result.fail(errors);
        }

        return employeeService.deactivate(employeeId);
    }

    /**
     * Reactivates an inactive employee.
     *
     * @param employeeId employee id
     * @return result
     */
    public Result<Void> activateEmployee(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "employee.activate.error.id.required");
            return Result.fail(errors);
        }

        return employeeService.activate(employeeId);
    }

    /**
     * Validates the basic employee form used for creation and edition.
     *
     * @param dto employee edit dto
     * @param requireId true when editing an existing employee
     * @return validation result
     */
    private Result<Void> validateBasicInformationForm(EmployeeEditDto dto, boolean requireId) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("employee", "employee.edit.error.form.invalid");
            return Result.fail(errors);
        }

        if (requireId && dto.getId() == null) {
            errors.put("employeeId", "employee.edit.error.id.required");
        }

        FormValidator.required(dto.getFirstName(), "firstName", "employee.edit.error.firstName.required", errors);
        FormValidator.required(dto.getLastName(), "lastName", "employee.edit.error.lastName.required", errors);
        FormValidator.required(dto.getEmail(), "email", "employee.edit.error.email.required", errors);
        FormValidator.required(dto.getEmployeeNumber(), "employeeNumber", "employee.edit.error.employeeNumber.required", errors);
        FormValidator.required(dto.getBirthDate(), "birthDate", "employee.edit.error.birthDate.required", errors);
        FormValidator.required(dto.getEmploymentStatus(), "employmentStatus", "employee.edit.error.employmentStatus.required", errors);
        FormValidator.required(dto.getCivilite(), "civilite", "employee.edit.error.civilite.required", errors);
        FormValidator.required(dto.getGender(), "gender", "employee.edit.error.gender.required", errors);

        FormValidator.lengthBetween(dto.getFirstName(), "firstName", "employee.edit.error.firstName.length", 1, 100, errors);
        FormValidator.lengthBetween(dto.getLastName(), "lastName", "employee.edit.error.lastName.length", 1, 100, errors);
        FormValidator.lengthBetween(dto.getEmail(), "email", "employee.edit.error.email.length", 5, 150, errors);
        FormValidator.lengthBetween(dto.getEmployeeNumber(), "employeeNumber", "employee.edit.error.employeeNumber.length", 1, 50, errors);
        FormValidator.lengthBetween(dto.getPhone(), "phone", "employee.edit.error.phone.length", 0, 20, errors);
        FormValidator.lengthBetween(dto.getPlaceOfBirth(), "placeOfBirth", "employee.edit.error.placeOfBirth.length", 0, 150, errors);
        FormValidator.ageBetween(dto.getBirthDate(), "birthDate", "employee.edit.error.birthDate.age", 15, 100, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    /**
     * Validates the employee address form.
     *
     * @param dto address edit dto
     * @return validation result
     */
    private Result<Void> validateAddressForm(AddressEditDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("address", "employee.address.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(dto.getStreetName(), "streetName", "employee.address.error.streetName.required", errors);
        FormValidator.lengthBetween(dto.getStreetName(), "streetName", "employee.address.error.streetName.length", 1, 50, errors);
        FormValidator.lengthBetween(dto.getStreetNumber(), "streetNumber", "employee.address.error.streetNumber.length", 0, 11, errors);
        FormValidator.lengthBetween(dto.getBoxNumber(), "boxNumber", "employee.address.error.boxNumber.length", 0, 5, errors);

        if (dto.getCityId() == null) {
            errors.put("cityId", "employee.address.error.city.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }
}
