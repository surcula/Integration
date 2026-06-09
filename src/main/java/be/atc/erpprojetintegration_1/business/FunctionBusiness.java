package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.interfaces.IFunctionService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FunctionBusiness {

    @Inject
    private IFunctionService functionService;

    /**
     * Retrieves all functions for the administration list.
     *
     * @return function list result
     */
    public Result<List<Function>> getAllFunctions() {
        return functionService.getAll();
    }

    /**
     * Retrieves a function by id.
     *
     * @param id function id
     * @return function result
     */
    public Result<Function> getFunctionById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "functions.error.id.required");
            return Result.fail(errors);
        }

        return functionService.getById(id);
    }

    /**
     * Creates or updates a function after validation.
     *
     * @param function function to save
     * @return saved function result
     */
    public Result<Function> saveFunction(Function function) {
        Result<Void> validationResult = validateFunction(function);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        trimFunctionFields(function);

        if (function.getId() == null) {
            function.setIsActive(true);
            return functionService.create(function);
        }

        return functionService.update(function);
    }

    /**
     * Updates function active status.
     *
     * @param id function id
     * @param active active status
     * @return operation result
     */
    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "functions.error.id.required");
            return Result.fail(errors);
        }

        return functionService.setActive(id, active);
    }

    private Result<Void> validateFunction(Function function) {
        Map<String, String> errors = new HashMap<>();

        if (function == null) {
            errors.put("function", "functions.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(function.getFunctionName(), "functionName", "functions.error.name.required", errors);
        FormValidator.lengthBetween(function.getFunctionName(), "functionName", "functions.error.name.length", 1, 150, errors);
        FormValidator.lengthBetween(function.getStatus(), "status", "functions.error.status.length", 0, 50, errors);

        if (function.getNumberOfOpenPositions() != null && function.getNumberOfOpenPositions() < 0) {
            errors.put("numberOfOpenPositions", "functions.error.openPositions.positive");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimFunctionFields(Function function) {
        function.setFunctionName(trim(function.getFunctionName()));
        function.setStatus(trim(function.getStatus()));
        function.setDescription(trim(function.getDescription()));
        function.setJobDescription(trim(function.getJobDescription()));
        function.setComments(trim(function.getComments()));

        if (function.getMandatory() == null) {
            function.setMandatory(false);
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
