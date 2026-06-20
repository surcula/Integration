package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EvaluationBusiness;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.enums.EvaluationStatus;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bean JSF du formulaire de creation et modification des evaluations.
 */
@Named
@ViewScoped
public class EvaluationEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(EvaluationEditBean.class);

    @Inject
    private EvaluationBusiness evaluationBusiness;

    @Inject
    private AuthBean authBean;

    private Integer evaluationId;
    private Integer selectedEmployeeId;
    private Evaluation evaluation;
    private List<Employee> employees;
    private String automaticEvaluatorName;
    private boolean automaticEvaluatorAvailable;
    private String selectedEvaluationType;
    private final List<String> evaluationTypes = Arrays.asList(
            "annual",
            "trial",
            "followUp",
            "exceptional",
            "sickLeaveReturn"
    );

    /**
     * Charge le formulaire selon le mode creation ou modification.
     */
    public void loadEvaluation() {
        loadEmployees();

        if (evaluationId == null) {
            if (!authBean.hasPermission("evaluation:create")) {
                evaluation = null;
                MessageUtils.addErrorMessage("evaluations.error.access.denied");
                return;
            }

            evaluation = new Evaluation();
            evaluation.setIsActive(true);
            evaluation.setStatus(EvaluationStatus.CREATED);
            return;
        }

        if (!authBean.hasPermission("evaluation:edit")) {
            evaluation = null;
            MessageUtils.addErrorMessage("evaluations.error.access.denied");
            return;
        }

        Result<Evaluation> result = evaluationBusiness.getEvaluationForEdit(evaluationId, authBean.getConnectedEmployee());

        if (result.isSuccess()) {
            evaluation = result.getData();
            selectedEmployeeId = evaluation.getEmployee() != null ? evaluation.getEmployee().getId() : null;
            automaticEvaluatorName = getEmployeeLabel(evaluation.getEvaluator());
            automaticEvaluatorAvailable = evaluation.getEvaluator() != null;
        } else {
            evaluation = null;
            MessageUtils.addErrorMessages(result, "evaluations.error.load");
        }
    }

    /**
     * Enregistre l'evaluation et retourne vers la liste.
     *
     * @return navigation JSF
     */
    public String save() {
        boolean createMode = evaluation != null && evaluation.getId() == null;

        if (createMode && !authBean.hasPermission("evaluation:create")) {
            MessageUtils.addErrorMessage("evaluations.error.access.denied");
            return null;
        }

        if (!createMode && !authBean.hasPermission("evaluation:edit")) {
            MessageUtils.addErrorMessage("evaluations.error.access.denied");
            return null;
        }

        if (selectedEvaluationType == null || selectedEvaluationType.trim().isEmpty()) {
            MessageUtils.addErrorMessage("evaluations.error.type.required");
            return null;
        }

        Result<Evaluation> result = evaluationBusiness.saveEvaluation(
                evaluation,
                selectedEmployeeId,
                authBean.getConnectedEmployee()
        );

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "evaluations.error.save");
            return null;
        }

        log.info((createMode ? "Evaluation creee" : "Evaluation modifiee")
                + " avec id: " + result.getData().getId());
        MessageUtils.addInfoMessage(createMode ? "evaluations.create.success" : "evaluations.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/evaluations?faces-redirect=true";
    }

    /**
     * Indique si le formulaire est en mode creation.
     *
     * @return true en creation
     */
    public boolean isCreateMode() {
        return evaluationId == null;
    }

    /**
     * Met a jour l'affichage de l'evaluateur automatique apres selection de l'employe.
     */
    public void updateEmployeeSelection() {
        updateAutomaticEvaluator();
        updateEvaluationName();
    }

    /**
     * Met a jour l'affichage de l'evaluateur automatique.
     */
    public void updateAutomaticEvaluator() {
        automaticEvaluatorName = null;
        automaticEvaluatorAvailable = false;

        if (selectedEmployeeId == null) {
            return;
        }

        Result<Employee> result = evaluationBusiness.getAutomaticEvaluator(selectedEmployeeId);

        if (result.isSuccess()) {
            automaticEvaluatorName = getEmployeeLabel(result.getData());
            automaticEvaluatorAvailable = true;
        }
    }

    /**
     * Propose un nom d'evaluation a partir du type et de l'employe.
     */
    public void updateEvaluationName() {
        if (evaluation == null || selectedEvaluationType == null || selectedEmployeeId == null) {
            return;
        }

        Employee selectedEmployee = findEmployeeById(selectedEmployeeId);

        if (selectedEmployee == null) {
            return;
        }

        evaluation.setEvaluationName(getEvaluationTypeLabel(selectedEvaluationType)
                + " - " + getEmployeeFirstLastName(selectedEmployee));
    }

    /**
     * Propose les employes correspondant a la recherche de l'utilisateur.
     *
     * @param query texte saisi dans l'autocomplete
     * @return employes correspondants
     */
    public List<Employee> completeEmployee(String query) {
        List<Employee> result = new ArrayList<>();

        if (employees == null) {
            return result;
        }

        String search = query == null ? "" : query.trim().toLowerCase();

        for (Employee employee : employees) {
            String fullName = getEmployeeLabel(employee).toLowerCase();

            if (search.isEmpty() || fullName.contains(search)) {
                result.add(employee);
            }
        }

        return result;
    }

    /**
     * Retourne le libelle complet d'un employe pour l'autocomplete.
     * La valeur peut etre un Employee, un identifiant Integer ou null.
     *
     * @param value employe ou identifiant
     * @return nom complet de l'employe
     */
    public String getEmployeeLabel(Object value) {
        if (value == null) {
            return "";
        }

        Employee employee = null;

        if (value instanceof Employee) {
            employee = (Employee) value;
        } else if (value instanceof Integer) {
            employee = findEmployeeById((Integer) value);
        }

        if (employee == null) {
            return "";
        }

        return (employee.getLastName() + " " + employee.getFirstName()).trim();
    }

    /**
     * Retourne le libelle traduit d'un statut.
     *
     * @param status statut
     * @return libelle traduit
     */
    public String getStatusLabel(EvaluationStatus status) {
        return status == null ? "" : MessageUtils.getMessage("evaluations.status." + status.getCode());
    }

    /**
     * Retourne la cle i18n d'un statut.
     *
     * @param status statut
     * @return cle du fichier messages
     */
    public String getStatusMessageKey(EvaluationStatus status) {
        return status == null ? "" : "evaluations.status." + status.getCode();
    }

    /**
     * Retourne le libelle traduit d'un type d'evaluation.
     *
     * @param type type d'evaluation
     * @return libelle traduit
     */
    public String getEvaluationTypeLabel(String type) {
        return type == null ? "" : MessageUtils.getMessage("evaluations.type." + type);
    }

    /**
     * Retourne la cle i18n d'un type d'evaluation.
     *
     * @param type type d'evaluation
     * @return cle du fichier messages
     */
    public String getEvaluationTypeMessageKey(String type) {
        return type == null ? "" : "evaluations.type." + type;
    }

    private void loadEmployees() {
        Result<List<Employee>> result = evaluationBusiness.getEmployeesForEvaluation(authBean.getConnectedEmployee());
        employees = result.isSuccess() ? result.getData() : new ArrayList<>();

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "evaluations.error.employees.load");
        }
    }

    private Employee findEmployeeById(Integer employeeId) {
        if (employeeId == null || employees == null) {
            return null;
        }

        for (Employee employee : employees) {
            if (employeeId.equals(employee.getId())) {
                return employee;
            }
        }

        return null;
    }

    private String getEmployeeFirstLastName(Employee employee) {
        if (employee == null) {
            return "";
        }

        return (employee.getFirstName() + " " + employee.getLastName()).trim();
    }

    public Integer getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Integer evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Integer getSelectedEmployeeId() {
        return selectedEmployeeId;
    }

    public void setSelectedEmployeeId(Integer selectedEmployeeId) {
        this.selectedEmployeeId = selectedEmployeeId;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public String getAutomaticEvaluatorName() {
        return automaticEvaluatorName;
    }

    public boolean isAutomaticEvaluatorAvailable() {
        return automaticEvaluatorAvailable;
    }

    public String getSelectedEvaluationType() {
        return selectedEvaluationType;
    }

    public void setSelectedEvaluationType(String selectedEvaluationType) {
        this.selectedEvaluationType = selectedEvaluationType;
    }

    public List<String> getEvaluationTypes() {
        return evaluationTypes;
    }

    public EvaluationStatus[] getEvaluationStatuses() {
        return EvaluationStatus.values();
    }
}
