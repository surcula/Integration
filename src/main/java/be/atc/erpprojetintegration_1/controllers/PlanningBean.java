package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentBusiness;
import be.atc.erpprojetintegration_1.business.AbsenceBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.business.PlanningBusiness;
import be.atc.erpprojetintegration_1.business.PublicHolidayBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class PlanningBean implements Serializable {

    private static final Logger log = Logger.getLogger(PlanningBean.class);
    private static final long serialVersionUID = 1L;

    @Inject
    private PlanningBusiness planningBusiness;

    @Inject
    private DepartmentBusiness departmentBusiness;

    @Inject
    private EmployeeBusiness employeeBusiness;

    @Inject
    private AuthBean authBean;

    @Inject
    private PublicHolidayBusiness publicHolidayBusiness;

    @Inject
    private AbsenceBusiness absenceBusiness;

    private ScheduleModel eventModel;
    private ScheduleEvent<Integer> selectedEvent;
    private Planning selectedPlanning;
    private List<Department> departments;
    private List<EmployeeListDto> employees;
    private Integer selectedDepartmentId;
    private Integer planningDepartmentFilterId;
    private List<Integer> selectedEmployeeIds;
    private String startHourValue;
    private String endHourValue;
    private boolean allDay;

    private String view = "timeGridWeek";
    private String locale = "fr";
    private String minTime = "06:00:00";
    private String maxTime = "22:00:00";
    private String slotDuration = "00:30:00";

    @PostConstruct
    public void init() {
        if (isCanManagePlanning()) {
            loadDepartments();
            loadEmployees();
        } else {
            departments = new ArrayList<>();
            employees = new ArrayList<>();
        }
        loadPlannings();
        prepareNewPlanning(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
    }

    public void loadPlannings() {
        eventModel = new DefaultScheduleModel();

        Result<List<Planning>> result = isCanManagePlanning()
                ? planningBusiness.getAllActive()
                : planningBusiness.getActiveByEmployee(getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger le planning.");
            return;
        }

        for (Planning planning : result.getData()) {
            if (matchesDepartmentFilter(planning)) {
                eventModel.addEvent(toScheduleEvent(planning));
            }
        }

        loadPublicHolidays();
        loadApprovedAbsences();
    }

    private void loadApprovedAbsences() {
        Result<List<Absence>> result = isCanManagePlanning()
                ? absenceBusiness.getAllActive()
                : absenceBusiness.getActiveByEmployee(getConnectedEmployeeId());
        if (!result.isSuccess()) {
            log.warn("Unable to load approved absences in planning");
            return;
        }
        for (Absence absence : result.getData()) {
            if (absence.getStatus() == be.atc.erpprojetintegration_1.enums.AbsenceStatus.APPROVED) {
                eventModel.addEvent(toScheduleEvent(absence));
            }
        }
    }

    private void loadPublicHolidays() {
        Result<List<PublicHoliday>> result = publicHolidayBusiness.getAll();
        if (!result.isSuccess()) {
            log.warn("Unable to load public holidays in planning");
            return;
        }

        for (PublicHoliday publicHoliday : result.getData()) {
            if (Boolean.TRUE.equals(publicHoliday.getIsActive())) {
                eventModel.addEvent(toScheduleEvent(publicHoliday));
            }
        }
    }

    private boolean matchesDepartmentFilter(Planning planning) {
        if (planningDepartmentFilterId == null) {
            return true;
        }

        return planning.getDepartment() != null
                && planningDepartmentFilterId.equals(planning.getDepartment().getId());
    }

    public void onDateSelect(SelectEvent<LocalDateTime> selectEvent) {
        if (!isCanManagePlanning()) {
            return;
        }
        prepareNewPlanning(selectEvent.getObject());
    }

    public void onEventSelect(SelectEvent<ScheduleEvent<Integer>> selectEvent) {
        selectedEvent = selectEvent.getObject();
        if ("PUBLIC_HOLIDAY".equals(selectedEvent.getGroupId()) || "ABSENCE".equals(selectedEvent.getGroupId())) {
            PrimeFaces.current().ajax().addCallbackParam("editablePlanningEvent", false);
            return;
        }

        PrimeFaces.current().ajax().addCallbackParam("editablePlanningEvent", true);
        selectedPlanning = loadPlanning(selectedEvent.getData());
        if (selectedPlanning == null) {
            return;
        }
        selectedDepartmentId = selectedPlanning.getDepartment() != null ? selectedPlanning.getDepartment().getId() : null;
        loadSelectedAssignments(selectedPlanning.getId());
        syncHourFields();
    }

    public void onEventMove(ScheduleEntryMoveEvent moveEvent) {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        ScheduleEvent<?> movedEvent = moveEvent.getScheduleEvent();
        if (movedEvent != null && movedEvent.getData() instanceof Integer) {
            Planning planning = loadPlanning((Integer) movedEvent.getData());
            if (planning == null) {
                return;
            }
            applyEventDates(planning, movedEvent);
            savePlanning(false);
        }
    }

    public void onEventResize(ScheduleEntryResizeEvent resizeEvent) {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        ScheduleEvent<?> resizedEvent = resizeEvent.getScheduleEvent();
        if (resizedEvent != null && resizedEvent.getData() instanceof Integer) {
            Planning planning = loadPlanning((Integer) resizedEvent.getData());
            if (planning == null) {
                return;
            }
            applyEventDates(planning, resizedEvent);
            savePlanning(false);
        }
    }

    public void savePlanning() {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a modifier le planning.");
            return;
        }
        savePlanning(true);
    }

    public void onTypeChange() {
        if (!isTypeSupportsAllDay()) {
            allDay = false;
        }
    }

    public void deletePlanning() {
        if (!isCanManagePlanning()) {
            addErrorMessage("Vous n'etes pas autorise a supprimer cet element.");
            return;
        }
        if (selectedPlanning == null || selectedPlanning.getId() == null) {
            return;
        }

        Result<Void> result = planningBusiness.deactivate(selectedPlanning.getId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de supprimer cet element du planning.");
            return;
        }

        addInfoMessage("Element supprime du planning.");
        loadPlannings();
        prepareNewPlanning(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0));
    }

    private void savePlanning(boolean showMessage) {
        if (!validateSelectedPlanning()) {
            return;
        }

        selectedPlanning.setDepartment(findSelectedDepartment());
        selectedPlanning.setIsActive(true);

        Result<Planning> result = planningBusiness.save(selectedPlanning, selectedEmployeeIds);
        if (!result.isSuccess()) {
            if (result.getErrors() != null && result.getErrors().containsKey("conflicts")) {
                addErrorMessage("Conflit de planning pour : " + result.getErrors().get("conflicts") + ".");
                return;
            }
            addErrorMessage("Impossible d'enregistrer le planning.");
            return;
        }

        if (showMessage) {
            addInfoMessage("Planning enregistre.");
        }

        loadPlannings();
        selectedPlanning = result.getData();
        selectedDepartmentId = selectedPlanning.getDepartment() != null ? selectedPlanning.getDepartment().getId() : null;
    }

    private boolean validateSelectedPlanning() {
        if (selectedPlanning == null) {
            addErrorMessage("Aucun element de planning selectionne.");
            return false;
        }

        try {
            syncPlanningHours();
        } catch (Exception ex) {
            addErrorMessage("Les heures doivent respecter le format HH:mm.");
            return false;
        }

        if (selectedPlanning.getDate() == null) {
            addErrorMessage("La date est obligatoire.");
            return false;
        }

        if (selectedPlanning.getNote() == null || selectedPlanning.getNote().trim().isEmpty()) {
            addErrorMessage("Le titre est obligatoire.");
            return false;
        }

        if (!allDay && (selectedPlanning.getStartHour() == null || selectedPlanning.getEndHour() == null)) {
            addErrorMessage("Les heures de debut et de fin sont obligatoires.");
            return false;
        }

        if (selectedPlanning.getStartHour() != null
                && selectedPlanning.getEndHour() != null
                && !selectedPlanning.getEndHour().isAfter(selectedPlanning.getStartHour())) {
            addErrorMessage("L'heure de fin doit etre apres l'heure de debut.");
            return false;
        }

        selectedPlanning.setNote(selectedPlanning.getNote().trim());
        selectedPlanning.setType(trim(selectedPlanning.getType()));
        selectedPlanning.setDescription(trim(selectedPlanning.getDescription()));
        return true;
    }

    private void prepareNewPlanning(LocalDateTime startDate) {
        selectedPlanning = new Planning();
        selectedPlanning.setDate(startDate.toLocalDate());
        selectedPlanning.setStartHour(startDate.toLocalTime());
        selectedPlanning.setEndHour(startDate.plusHours(1).toLocalTime());
        selectedPlanning.setType("SERVICE");
        selectedPlanning.setIsActive(true);
        allDay = false;
        selectedDepartmentId = null;
        selectedEmployeeIds = new ArrayList<>();
        selectedEvent = null;
        syncHourFields();
    }

    private ScheduleEvent<Integer> toScheduleEvent(Planning planning) {
        LocalDateTime startDate = toStartDate(planning);
        LocalDateTime endDate = toEndDate(planning, startDate);

        return DefaultScheduleEvent.<Integer>builder()
                .id(String.valueOf(planning.getId()))
                .title(buildTitle(planning))
                .startDate(startDate)
                .endDate(endDate)
                .description(planning.getDescription())
                .data(planning.getId())
                .allDay(planning.getStartHour() == null)
                .borderColor(resolveColor(planning.getType()))
                .backgroundColor(resolveColor(planning.getType()))
                .textColor(resolveTextColor(planning.getType()))
                .build();
    }

    private ScheduleEvent<Integer> toScheduleEvent(PublicHoliday publicHoliday) {
        LocalDateTime startDate = publicHoliday.getHolidayDate().atStartOfDay();
        return DefaultScheduleEvent.<Integer>builder()
                .id("holiday-" + publicHoliday.getId())
                .groupId("PUBLIC_HOLIDAY")
                .title(publicHoliday.getName())
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .description(publicHoliday.getDescription())
                .allDay(true)
                .editable(false)
                .draggable(false)
                .resizable(false)
                .borderColor("#ef4444")
                .backgroundColor("#ef4444")
                .textColor("#ffffff")
                .build();
    }

    private ScheduleEvent<Integer> toScheduleEvent(Absence absence) {
        boolean isAllDay = Boolean.TRUE.equals(absence.getAllDay());
        LocalDateTime startDate = isAllDay ? absence.getStartDate().atStartOfDay()
                : absence.getStartDate().atTime(absence.getStartHour());
        LocalDateTime endDate = isAllDay ? absence.getEndDate().plusDays(1).atStartOfDay()
                : absence.getEndDate().atTime(absence.getEndHour());
        String employeeName = absence.getEmployee().getFirstName() + " " + absence.getEmployee().getLastName();
        return DefaultScheduleEvent.<Integer>builder()
                .id("absence-" + absence.getId())
                .groupId("ABSENCE")
                .title(absence.getType().getLabel() + " - " + employeeName)
                .startDate(startDate)
                .endDate(endDate)
                .description(absence.getComment())
                .allDay(isAllDay)
                .editable(false)
                .borderColor("#7c3aed")
                .backgroundColor("#7c3aed")
                .textColor("#ffffff")
                .build();
    }

    private Planning loadPlanning(Integer planningId) {
        Result<Planning> result = isCanManagePlanning()
                ? planningBusiness.getById(planningId)
                : planningBusiness.getByIdForEmployee(planningId, getConnectedEmployeeId());
        if (!result.isSuccess()) {
            addErrorMessage("Impossible de charger cet element du planning.");
            return null;
        }
        return result.getData();
    }

    private Integer getConnectedEmployeeId() {
        return authBean.getConnectedEmployee() != null
                ? authBean.getConnectedEmployee().getId()
                : null;
    }

    private String buildTitle(Planning planning) {
        StringBuilder title = new StringBuilder();
        title.append(planning.getNote() != null ? planning.getNote() : "Planning");

        if (planning.getDepartment() != null && planning.getDepartment().getDepartmentName() != null) {
            title.append(" - ").append(planning.getDepartment().getDepartmentName());
        }

        return title.toString();
    }

    private LocalDateTime toStartDate(Planning planning) {
        LocalDate date = planning.getDate();
        LocalTime startHour = planning.getStartHour() != null ? planning.getStartHour() : LocalTime.MIN;
        return LocalDateTime.of(date, startHour);
    }

    private LocalDateTime toEndDate(Planning planning, LocalDateTime startDate) {
        if (planning.getEndHour() == null) {
            return planning.getStartHour() == null ? startDate.plusDays(1) : startDate.plusHours(1);
        }

        return LocalDateTime.of(planning.getDate(), planning.getEndHour());
    }

    private void applyEventDates(Planning planning, ScheduleEvent<?> scheduleEvent) {
        planning.setDate(scheduleEvent.getStartDate().toLocalDate());
        planning.setStartHour(scheduleEvent.isAllDay() ? null : scheduleEvent.getStartDate().toLocalTime());
        planning.setEndHour(scheduleEvent.isAllDay() ? null : scheduleEvent.getEndDate().toLocalTime());
        allDay = scheduleEvent.isAllDay() && supportsAllDay(planning.getType());
        selectedPlanning = planning;
        selectedDepartmentId = planning.getDepartment() != null ? planning.getDepartment().getId() : null;
        loadSelectedAssignments(planning.getId());
        syncHourFields();
    }

    private void syncHourFields() {
        allDay = selectedPlanning != null
                && selectedPlanning.getStartHour() == null
                && selectedPlanning.getEndHour() == null
                && supportsAllDay(selectedPlanning.getType());
        startHourValue = selectedPlanning != null && selectedPlanning.getStartHour() != null
                ? selectedPlanning.getStartHour().toString()
                : "";
        endHourValue = selectedPlanning != null && selectedPlanning.getEndHour() != null
                ? selectedPlanning.getEndHour().toString()
                : "";
    }

    private void syncPlanningHours() {
        if (allDay && isTypeSupportsAllDay()) {
            selectedPlanning.setStartHour(null);
            selectedPlanning.setEndHour(null);
            return;
        }

        selectedPlanning.setStartHour(parseHour(startHourValue));
        selectedPlanning.setEndHour(parseHour(endHourValue));
    }

    private boolean supportsAllDay(String type) {
        if (type == null) {
            return false;
        }

        switch (type.trim().toUpperCase()) {
            case "REPOS":
            case "RECUP":
            case "RECUPERATION":
            case "FERIE":
            case "HOLIDAY":
            case "VACANCES":
            case "CONGE":
                return true;
            default:
                return false;
        }
    }

    private LocalTime parseHour(String value) {
        if (value == null || value.trim().isEmpty() || value.contains("_")) {
            return null;
        }

        return LocalTime.parse(value.trim());
    }

    private void loadDepartments() {
        Result<List<Department>> result = departmentBusiness.getAllDepartments();
        departments = result.isSuccess() ? result.getData() : new ArrayList<Department>();

        if (!result.isSuccess()) {
            log.warn("Unable to load departments for planning page");
        }
    }

    private void loadEmployees() {
        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList(false);
        employees = result.isSuccess() ? result.getData() : new ArrayList<EmployeeListDto>();

        if (!result.isSuccess()) {
            log.warn("Unable to load employees for planning page");
        }
    }

    private void loadSelectedAssignments(Integer planningId) {
        Result<List<Employee>> result = planningBusiness.getAssignedEmployees(planningId);
        selectedEmployeeIds = result.isSuccess()
                ? result.getData().stream().map(Employee::getId).collect(Collectors.toList())
                : new ArrayList<Integer>();

        if (!result.isSuccess()) {
            log.warn("Unable to load employee assignments for planning id: " + planningId);
        }
    }

    private Department findSelectedDepartment() {
        if (selectedDepartmentId == null || departments == null) {
            return null;
        }

        for (Department department : departments) {
            if (selectedDepartmentId.equals(department.getId())) {
                return department;
            }
        }

        return null;
    }

    private String resolveColor(String type) {
        if (type == null) {
            return "#3b82f6";
        }

        switch (type.trim().toUpperCase()) {
            case "REPOS":
                return "#22c55e";
            case "RECUP":
            case "RECUPERATION":
                return "#facc15";
            case "FERIE":
            case "HOLIDAY":
                return "#ef4444";
            case "VACANCES":
            case "CONGE":
                return "#f97316";
            default:
                return "#3b82f6";
        }
    }

    private String resolveTextColor(String type) {
        if (type != null) {
            String normalizedType = type.trim().toUpperCase();
            if ("RECUP".equals(normalizedType) || "RECUPERATION".equals(normalizedType)) {
                return "#422006";
            }
        }
        return "#ffffff";
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public Planning getSelectedPlanning() {
        return selectedPlanning;
    }

    public void setSelectedPlanning(Planning selectedPlanning) {
        this.selectedPlanning = selectedPlanning;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<EmployeeListDto> getEmployees() {
        return employees;
    }

    public Integer getSelectedDepartmentId() {
        return selectedDepartmentId;
    }

    public void setSelectedDepartmentId(Integer selectedDepartmentId) {
        this.selectedDepartmentId = selectedDepartmentId;
    }

    public Integer getPlanningDepartmentFilterId() {
        return planningDepartmentFilterId;
    }

    public void setPlanningDepartmentFilterId(Integer planningDepartmentFilterId) {
        this.planningDepartmentFilterId = planningDepartmentFilterId;
    }

    public List<Integer> getSelectedEmployeeIds() {
        return selectedEmployeeIds;
    }

    public void setSelectedEmployeeIds(List<Integer> selectedEmployeeIds) {
        this.selectedEmployeeIds = selectedEmployeeIds;
    }

    public String getStartHourValue() {
        return startHourValue;
    }

    public void setStartHourValue(String startHourValue) {
        this.startHourValue = startHourValue;
    }

    public String getEndHourValue() {
        return endHourValue;
    }

    public void setEndHourValue(String endHourValue) {
        this.endHourValue = endHourValue;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public boolean isTypeSupportsAllDay() {
        return selectedPlanning != null && supportsAllDay(selectedPlanning.getType());
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getLocale() {
        return locale;
    }

    public String getMinTime() {
        return minTime;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public String getSlotDuration() {
        return slotDuration;
    }

    public boolean isCanManagePlanning() {
        return authBean != null && authBean.isHrOrAdmin();
    }
}
