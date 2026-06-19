package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.entities.Superior;
import be.atc.erpprojetintegration_1.enums.EvaluationStatus;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IEvaluationService;
import be.atc.erpprojetintegration_1.interfaces.ISuperiorService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Couche Business des evaluations.
 * Elle centralise les validations et les regles d'acces selon le role connecte.
 */
@ApplicationScoped
public class EvaluationBusiness {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_HR = "HR";
    private static final String ROLE_SERVICE_MANAGER = "SERVICE_MANAGER";

    @Inject
    private IEvaluationService evaluationService;

    @Inject
    private IEmployeeService employeeService;

    @Inject
    private ISuperiorService superiorService;

    /**
     * Recupere les evaluations visibles pour l'utilisateur connecte.
     *
     * @param connectedEmployee utilisateur connecte
     * @return resultat contenant les evaluations visibles
     */
    public Result<List<Evaluation>> getVisibleEvaluations(ConnectedEmployeeDto connectedEmployee) {
        if (connectedEmployee == null) {
            return Result.ok(new ArrayList<>());
        }

        Result<List<Evaluation>> result = evaluationService.getAllActive();

        if (!result.isSuccess()) {
            return result;
        }

        if (isHrOrAdmin(connectedEmployee)) {
            return result;
        }

        if (isServiceManager(connectedEmployee)) {
            List<Integer> supervisedEmployeeIds = getSupervisedEmployeeIds(connectedEmployee.getId());
            List<Evaluation> evaluations = new ArrayList<>();

            for (Evaluation evaluation : result.getData()) {
                if (isOwnEvaluation(evaluation, connectedEmployee.getId())
                        || supervisedEmployeeIds.contains(getEmployeeId(evaluation))) {
                    evaluations.add(evaluation);
                }
            }

            return Result.ok(evaluations);
        }

        List<Evaluation> ownEvaluations = new ArrayList<>();

        for (Evaluation evaluation : result.getData()) {
            if (isOwnEvaluation(evaluation, connectedEmployee.getId())) {
                ownEvaluations.add(evaluation);
            }
        }

        return Result.ok(ownEvaluations);
    }

    /**
     * Recupere une evaluation si l'utilisateur a le droit de la consulter.
     *
     * @param id identifiant de l'evaluation
     * @param connectedEmployee utilisateur connecte
     * @return resultat contenant l'evaluation
     */
    public Result<Evaluation> getEvaluationForEdit(Integer id, ConnectedEmployeeDto connectedEmployee) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "evaluations.error.id.required");
            return Result.fail(errors);
        }

        Result<Evaluation> result = evaluationService.getById(id);

        if (!result.isSuccess()) {
            return result;
        }

        if (!canEdit(result.getData(), connectedEmployee)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("access", "evaluations.error.access.denied");
            return Result.fail(errors);
        }

        return result;
    }

    /**
     * Cree ou modifie une evaluation apres validation.
     *
     * @param evaluation evaluation saisie
     * @param employeeId identifiant de l'employe evalue
     * @param connectedEmployee utilisateur connecte
     * @return resultat contenant l'evaluation enregistree
     */
    public Result<Evaluation> saveEvaluation(Evaluation evaluation, Integer employeeId,
                                             ConnectedEmployeeDto connectedEmployee) {
        if (evaluation != null && evaluation.getId() == null) {
            evaluation.setStatus(EvaluationStatus.CREATED);
        }

        Result<Void> validationResult = validateEvaluation(evaluation, employeeId, connectedEmployee);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<Employee> employeeResult = employeeService.getById(employeeId);

        if (!employeeResult.isSuccess()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employee", "evaluations.error.employee.notFound");
            return Result.fail(errors);
        }

        Result<Superior> superiorResult = superiorService.getActiveByEmployeeId(employeeId);

        if (!superiorResult.isSuccess()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("superior", "evaluations.error.superior.notFound");
            return Result.fail(errors);
        }

        trimEvaluationFields(evaluation);
        evaluation.setEmployee(employeeResult.getData());
        evaluation.setEvaluator(superiorResult.getData().getSuperior());

        if (evaluation.getEvaluator() == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("superior", "evaluations.error.superior.notFound");
            return Result.fail(errors);
        }

        if (isAdminDefault(evaluation.getEvaluator())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("superior", "evaluations.error.superior.defaultNotAllowed");
            return Result.fail(errors);
        }

        if (evaluation.getEmployee().getId().equals(evaluation.getEvaluator().getId())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("evaluator", "evaluations.error.same.employee.evaluator");
            return Result.fail(errors);
        }

        if (evaluation.getStatus() == null) {
            evaluation.setStatus(EvaluationStatus.CREATED);
        }

        if (evaluation.getId() == null) {
            evaluation.setIsActive(true);
            return evaluationService.create(evaluation);
        }

        return evaluationService.update(evaluation);
    }

    /**
     * Supprime logiquement une evaluation si l'utilisateur est autorise.
     *
     * @param id identifiant de l'evaluation
     * @param connectedEmployee utilisateur connecte
     * @return resultat de l'operation
     */
    public Result<Void> softDelete(Integer id, ConnectedEmployeeDto connectedEmployee) {
        Result<Evaluation> result = evaluationService.getById(id);

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        if (!canEdit(result.getData(), connectedEmployee)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("access", "evaluations.error.access.denied");
            return Result.fail(errors);
        }

        return evaluationService.softDelete(id);
    }

    /**
     * Recupere les employes disponibles comme employes evalues.
     *
     * @param connectedEmployee utilisateur connecte
     * @return resultat contenant les employes
     */
    public Result<List<Employee>> getEmployeesForEvaluation(ConnectedEmployeeDto connectedEmployee) {
        if (isHrOrAdmin(connectedEmployee)) {
            Result<List<Employee>> result = employeeService.getAllActive();

            if (!result.isSuccess()) {
                return result;
            }

            return Result.ok(filterEvaluableEmployees(result.getData()));
        }

        if (isServiceManager(connectedEmployee)) {
            Result<List<Superior>> result = superiorService.getAll();

            if (!result.isSuccess()) {
                return Result.fail(result.getErrors());
            }

            List<Employee> employees = new ArrayList<>();

            for (Superior superior : result.getData()) {
                if (isActiveSuperiorOfConnectedManager(superior, connectedEmployee.getId())) {
                    Employee employee = superior.getEmployee();

                    if (employee != null
                            && Boolean.TRUE.equals(employee.getIsActive())
                            && !isAdminDefault(employee)) {
                        employees.add(employee);
                    }
                }
            }

            return Result.ok(employees);
        }

        return Result.ok(new ArrayList<>());
    }

    /**
     * Recupere l'evaluateur automatique d'un employe a partir de son superieur actif.
     *
     * @param employeeId identifiant de l'employe evalue
     * @return resultat contenant l'evaluateur automatique
     */
    public Result<Employee> getAutomaticEvaluator(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employee", "evaluations.error.employee.required");
            return Result.fail(errors);
        }

        Result<Superior> superiorResult = superiorService.getActiveByEmployeeId(employeeId);

        if (!superiorResult.isSuccess()
                || superiorResult.getData() == null
                || superiorResult.getData().getSuperior() == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("superior", "evaluations.error.superior.notFound");
            return Result.fail(errors);
        }

        Employee evaluator = superiorResult.getData().getSuperior();

        if (isAdminDefault(evaluator)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("superior", "evaluations.error.superior.defaultNotAllowed");
            return Result.fail(errors);
        }

        return Result.ok(evaluator);
    }

    /**
     * Indique si l'utilisateur peut creer une evaluation.
     *
     * @param connectedEmployee utilisateur connecte
     * @return true si la creation est autorisee
     */
    public boolean canCreate(ConnectedEmployeeDto connectedEmployee) {
        return isHrOrAdmin(connectedEmployee) || isServiceManager(connectedEmployee);
    }

    /**
     * Indique si l'utilisateur peut modifier une evaluation.
     *
     * @param evaluation evaluation concernee
     * @param connectedEmployee utilisateur connecte
     * @return true si la modification est autorisee
     */
    public boolean canEdit(Evaluation evaluation, ConnectedEmployeeDto connectedEmployee) {
        if (evaluation == null || connectedEmployee == null) {
            return false;
        }

        if (isOwnEvaluation(evaluation, connectedEmployee.getId())) {
            return false;
        }

        if (isHrOrAdmin(connectedEmployee)) {
            return true;
        }

        return isServiceManager(connectedEmployee)
                && getSupervisedEmployeeIds(connectedEmployee.getId()).contains(getEmployeeId(evaluation));
    }

    private Result<Void> validateEvaluation(Evaluation evaluation, Integer employeeId,
                                            ConnectedEmployeeDto connectedEmployee) {
        Map<String, String> errors = new HashMap<>();

        if (evaluation == null) {
            errors.put("evaluation", "evaluations.error.form.invalid");
            return Result.fail(errors);
        }

        if (!canCreate(connectedEmployee)) {
            errors.put("access", "evaluations.error.access.denied");
        }

        FormValidator.required(evaluation.getEvaluationName(), "evaluationName",
                "evaluations.error.name.required", errors);
        FormValidator.lengthBetween(evaluation.getEvaluationName(), "evaluationName",
                "evaluations.error.name.length", 1, 200, errors);

        if (evaluation.getStatus() == null) {
            errors.put("status", "evaluations.error.status.required");
        }

        if (employeeId == null) {
            errors.put("employee", "evaluations.error.employee.required");
        }

        if (connectedEmployee != null && employeeId != null && employeeId.equals(connectedEmployee.getId())) {
            errors.put("employee", "evaluations.error.self.edit");
        }

        if (evaluation.getPeriodStart() != null && evaluation.getPeriodEnd() != null
                && evaluation.getPeriodEnd().isBefore(evaluation.getPeriodStart())) {
            errors.put("periodEnd", "evaluations.error.period.invalid");
        }

        if (evaluation.getEvaluationDate() == null) {
            errors.put("evaluationDate", "evaluations.error.date.required");
        }

        if (evaluation.getPeriodStart() == null) {
            errors.put("periodStart", "evaluations.error.periodStart.required");
        }

        if (evaluation.getPeriodEnd() == null) {
            errors.put("periodEnd", "evaluations.error.periodEnd.required");
        }

        if (evaluation.getId() != null && !canEdit(evaluation, connectedEmployee)) {
            errors.put("access", "evaluations.error.access.denied");
        }

        if (isServiceManager(connectedEmployee) && employeeId != null
                && !getSupervisedEmployeeIds(connectedEmployee.getId()).contains(employeeId)) {
            errors.put("employee", "evaluations.error.employee.notSupervised");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private List<Integer> getSupervisedEmployeeIds(Integer superiorId) {
        Result<List<Superior>> result = superiorService.getAll();

        if (!result.isSuccess()) {
            return new ArrayList<>();
        }

        List<Integer> employeeIds = new ArrayList<>();

        for (Superior superior : result.getData()) {
            if (isActiveSuperiorOfConnectedManager(superior, superiorId)
                    && superior.getEmployee() != null) {
                employeeIds.add(superior.getEmployee().getId());
            }
        }

        return employeeIds;
    }

    private boolean isHrOrAdmin(ConnectedEmployeeDto connectedEmployee) {
        return hasRole(connectedEmployee, ROLE_ADMIN) || hasRole(connectedEmployee, ROLE_HR);
    }

    private boolean isServiceManager(ConnectedEmployeeDto connectedEmployee) {
        return hasRole(connectedEmployee, ROLE_SERVICE_MANAGER);
    }

    private boolean hasRole(ConnectedEmployeeDto connectedEmployee, String roleName) {
        return connectedEmployee != null && roleName.equals(connectedEmployee.getRoleName());
    }

    private boolean isOwnEvaluation(Evaluation evaluation, Integer employeeId) {
        return employeeId != null && employeeId.equals(getEmployeeId(evaluation));
    }

    private Integer getEmployeeId(Evaluation evaluation) {
        return evaluation != null && evaluation.getEmployee() != null ? evaluation.getEmployee().getId() : null;
    }

    private List<Employee> filterEvaluableEmployees(List<Employee> employees) {
        if (employees == null) {
            return new ArrayList<>();
        }

        List<Employee> filteredEmployees = new ArrayList<>();

        for (Employee employee : employees) {
            if (!isAdminDefault(employee)) {
                filteredEmployees.add(employee);
            }
        }

        return filteredEmployees;
    }

    private boolean isActiveSuperiorOfConnectedManager(Superior superior, Integer superiorId) {
        return superior != null
                && Boolean.TRUE.equals(superior.getIsActive())
                && superior.getSuperior() != null
                && superiorId != null
                && superiorId.equals(superior.getSuperior().getId());
    }

    private boolean isAdminDefault(Employee employee) {
        if (employee == null) {
            return false;
        }

        return "Admin".equalsIgnoreCase(trim(employee.getFirstName()))
                && "Default".equalsIgnoreCase(trim(employee.getLastName()));
    }

    private void trimEvaluationFields(Evaluation evaluation) {
        evaluation.setEvaluationName(trim(evaluation.getEvaluationName()));
        evaluation.setComments(trim(evaluation.getComments()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
