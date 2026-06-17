package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.enums.AbsenceStatus;
import be.atc.erpprojetintegration_1.interfaces.IAbsenceService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.enums.AbsenceType;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@ApplicationScoped
public class AbsenceBusiness {
    @Inject private IAbsenceService absenceService;
    @Inject private IEmployeeService employeeService;
    @Inject private IDepartmentHeadService departmentHeadService;
    @Inject private IEmployeeDepartmentService employeeDepartmentService;

    public Result<List<Absence>> getAllActive() { return absenceService.getAllActive(); }
    public Result<List<Absence>> getActiveByEmployee(Integer employeeId) { return absenceService.getActiveByEmployee(employeeId); }
    public Result<Absence> getById(Integer id) { return absenceService.getById(id); }

    public Result<List<Absence>> getAccessible(Integer actorEmployeeId, boolean hrOrAdmin) {
        if (hrOrAdmin) return absenceService.getAllActive();
        Result<List<DepartmentHead>> heads = departmentHeadService.getActiveByEmployeeId(actorEmployeeId);
        if (!heads.isSuccess()) return Result.fail(heads.getErrors());
        if (heads.getData().isEmpty()) return absenceService.getActiveByEmployee(actorEmployeeId);
        Result<List<Absence>> all = absenceService.getAllActive();
        if (!all.isSuccess()) return all;
        List<Integer> managedIds = new ArrayList<>();
        for (DepartmentHead head : heads.getData()) managedIds.add(head.getDepartment().getId());
        List<Absence> accessible = new ArrayList<>();
        Map<Integer, Integer> employeeDeptCache = new HashMap<>();
        for (Absence absence : all.getData()) {
            Integer empId = absence.getEmployee().getId();
            Integer deptId = employeeDeptCache.get(empId);
            if (deptId == null) {
                Result<EmployeeDepartment> assignment = employeeDepartmentService
                        .getActiveEmployeeDepartmentByEmployeeId(empId);
                if (!assignment.isSuccess()) continue;
                deptId = assignment.getData().getDepartment().getId();
                employeeDeptCache.put(empId, deptId);
            }
            if (managedIds.contains(deptId)) accessible.add(absence);
        }
        return Result.ok(accessible);
    }

    public Result<Boolean> isDepartmentHead(Integer employeeId) {
        Result<List<DepartmentHead>> result = departmentHeadService.getActiveByEmployeeId(employeeId);
        return result.isSuccess() ? Result.ok(!result.getData().isEmpty()) : Result.fail(result.getErrors());
    }

    public Result<Absence> saveRequest(Absence absence, Integer employeeId, boolean submit) {
        Result<Void> validation = validate(absence);
        if (!validation.isSuccess()) return Result.fail(validation.getErrors());

        Result<Employee> employeeResult = employeeService.getById(employeeId);
        if (!employeeResult.isSuccess()) return Result.fail(employeeResult.getErrors());
        absence.setEmployee(employeeResult.getData());

        Result<List<Absence>> conflicts = absenceService.getBlockingAbsences(employeeId,
                absence.getStartDate(), absence.getEndDate(), absence.getId());
        if (!conflicts.isSuccess()) return Result.fail(conflicts.getErrors());
        for (Absence existing : conflicts.getData()) {
            if (overlaps(absence, existing)) return Result.fail(error("conflict", "Une autre absence couvre deja cette periode."));
        }

        if (absence.getId() == null) absence.setCreatedAt(LocalDateTime.now());
        absence.setStatus(submit ? AbsenceStatus.PENDING : AbsenceStatus.DRAFT);
        absence.setIsActive(true);
        normalize(absence);
        return absenceService.save(absence);
    }

    public Result<Absence> review(Integer absenceId, Integer reviewerId, AbsenceStatus decision,
                                  String comment, boolean hrOrAdmin) {
        if (decision != AbsenceStatus.APPROVED && decision != AbsenceStatus.REFUSED) {
            return Result.fail(error("status", "Decision invalide."));
        }
        Result<Absence> absenceResult = absenceService.getById(absenceId);
        if (!absenceResult.isSuccess()) return absenceResult;
        Absence absence = absenceResult.getData();
        if (absence.getStatus() != AbsenceStatus.PENDING) return Result.fail(error("status", "Cette demande n'est plus en attente."));

        if (absence.getType() == AbsenceType.UNPAID_LEAVE) {
            if (!hrOrAdmin) return Result.fail(error("access", "Le conge sans solde est traite uniquement par la RH."));
        } else if (!hrOrAdmin && !managesEmployee(reviewerId, absence.getEmployee().getId())) {
            return Result.fail(error("access", "Vous ne pouvez traiter que les demandes de votre equipe."));
        }
        if (absence.getType() == AbsenceType.SICKNESS && decision == AbsenceStatus.APPROVED
                && !Boolean.TRUE.equals(absence.getCertificateValidated())) {
            return Result.fail(error("certificate", "Le certificat doit etre valide par la RH avant l'approbation."));
        }

        if (decision == AbsenceStatus.APPROVED) {
            Result<List<Planning>> plannings = absenceService.getEmployeePlannings(absence.getEmployee().getId(), absence.getStartDate(), absence.getEndDate());
            if (!plannings.isSuccess()) return Result.fail(plannings.getErrors());
            for (Planning planning : plannings.getData()) {
                if (overlaps(absence, planning)) return Result.fail(error("planningConflict", "Un planning existe deja sur cette periode."));
            }
        }

        Result<Employee> reviewer = employeeService.getById(reviewerId);
        if (!reviewer.isSuccess()) return Result.fail(reviewer.getErrors());
        absence.setReviewer(reviewer.getData());
        absence.setStatus(decision);
        absence.setReviewComment(trim(comment));
        absence.setReviewedAt(LocalDateTime.now());
        return absenceService.save(absence);
    }

    public Result<Absence> validateCertificate(Integer absenceId, Integer reviewerId) {
        Result<Absence> absenceResult = absenceService.getById(absenceId);
        if (!absenceResult.isSuccess()) return absenceResult;
        Absence absence = absenceResult.getData();
        if (absence.getDocumentPath() == null || absence.getDocumentPath().trim().isEmpty()) {
            return Result.fail(error("certificate", "Aucun certificat n'a ete depose."));
        }
        Result<Employee> reviewer = employeeService.getById(reviewerId);
        if (!reviewer.isSuccess()) return Result.fail(reviewer.getErrors());
        absence.setCertificateValidated(true);
        absence.setCertificateValidatedBy(reviewer.getData());
        absence.setCertificateValidatedAt(LocalDateTime.now());
        return absenceService.save(absence);
    }

    public Result<Integer> refuseOverdueSickness() {
        Result<List<Absence>> result = absenceService
                .getPendingSicknessWithoutCertificateBefore(LocalDate.now());
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        int refused = 0;
        for (Absence absence : result.getData()) {
            absence.setStatus(AbsenceStatus.REFUSED);
            absence.setReviewComment("Refus automatique : certificat non depose dans les 24 heures suivant la date de debut.");
            absence.setReviewedAt(LocalDateTime.now());
            Result<Absence> saved = absenceService.save(absence);
            if (saved.isSuccess()) refused++;
        }
        return Result.ok(refused);
    }

    private boolean managesEmployee(Integer managerId, Integer employeeId) {
        Result<List<DepartmentHead>> heads = departmentHeadService.getActiveByEmployeeId(managerId);
        Result<EmployeeDepartment> assignment = employeeDepartmentService
                .getActiveEmployeeDepartmentByEmployeeId(employeeId);
        if (!heads.isSuccess() || !assignment.isSuccess()) return false;
        Integer employeeDepartmentId = assignment.getData().getDepartment().getId();
        for (DepartmentHead head : heads.getData()) {
            if (employeeDepartmentId.equals(head.getDepartment().getId())) return true;
        }
        return false;
    }

    public Result<Absence> cancel(Integer absenceId, Integer employeeId, boolean manager) {
        Result<Absence> result = absenceService.getById(absenceId);
        if (!result.isSuccess()) return result;
        Absence absence = result.getData();
        if (!manager && !absence.getEmployee().getId().equals(employeeId)) return Result.fail(error("access", "Acces refuse."));
        if (absence.getStatus() == AbsenceStatus.REFUSED || absence.getStatus() == AbsenceStatus.CANCELLED) {
            return Result.fail(error("status", "Cette demande ne peut plus etre annulee."));
        }
        absence.setStatus(AbsenceStatus.CANCELLED);
        return absenceService.save(absence);
    }

    public boolean blocksPlanning(Absence absence, LocalDate date, LocalTime start, LocalTime end) {
        if (absence == null || absence.getStatus() != AbsenceStatus.APPROVED || date == null) return false;
        LocalDateTime planningStart = date.atTime(start == null ? LocalTime.MIN : start);
        LocalDateTime planningEnd = start == null || end == null
                ? date.plusDays(1).atStartOfDay()
                : date.plusDays(end.isBefore(start) ? 1 : 0).atTime(end);
        LocalDateTime absenceStart = absence.getStartDate().atTime(
                Boolean.TRUE.equals(absence.getAllDay()) ? LocalTime.MIN : absence.getStartHour());
        LocalDateTime absenceEnd = Boolean.TRUE.equals(absence.getAllDay())
                ? absence.getEndDate().plusDays(1).atStartOfDay()
                : absence.getEndDate().atTime(absence.getEndHour());
        return planningStart.isBefore(absenceEnd) && planningEnd.isAfter(absenceStart);
    }

    public Result<List<String>> findPlanningConflicts(List<Integer> employeeIds, LocalDate date, LocalTime start, LocalTime end) {
        List<String> names = new ArrayList<>();
        if (employeeIds == null) return Result.ok(names);
        for (Integer employeeId : employeeIds) {
            LocalDate endDate = start != null && end != null && end.isBefore(start) ? date.plusDays(1) : date;
            Result<List<Absence>> result = absenceService.getBlockingAbsences(employeeId, date, endDate, null);
            if (!result.isSuccess()) return Result.fail(result.getErrors());
            for (Absence absence : result.getData()) {
                if (absence.getStatus() == AbsenceStatus.APPROVED && blocksPlanning(absence, date, start, end)) {
                    names.add(absence.getEmployee().getFirstName() + " " + absence.getEmployee().getLastName());
                    break;
                }
            }
        }
        return Result.ok(names);
    }

    private Result<Void> validate(Absence absence) {
        Map<String, String> errors = new HashMap<>();
        if (absence == null) return Result.fail(error("absence", "Formulaire invalide."));
        if (absence.getType() == null) errors.put("type", "Le type est obligatoire.");
        if (absence.getStartDate() == null) errors.put("startDate", "La date de debut est obligatoire.");
        if (absence.getEndDate() == null) errors.put("endDate", "La date de fin est obligatoire.");
        if (absence.getStartDate() != null && absence.getEndDate() != null && absence.getEndDate().isBefore(absence.getStartDate()))
            errors.put("dates", "La date de fin doit suivre la date de debut.");
        if (!Boolean.TRUE.equals(absence.getAllDay())) {
            if (absence.getStartHour() == null || absence.getEndHour() == null) errors.put("hours", "Les heures sont obligatoires pour une absence partielle.");
            else if (absence.getStartDate() != null && absence.getStartDate().equals(absence.getEndDate()) && !absence.getEndHour().isAfter(absence.getStartHour()))
                errors.put("hours", "L'heure de fin doit suivre l'heure de debut.");
        }
        return errors.isEmpty() ? Result.ok() : Result.fail(errors);
    }

    private void normalize(Absence absence) {
        if (Boolean.TRUE.equals(absence.getAllDay())) { absence.setStartHour(null); absence.setEndHour(null); }
        absence.setName(absence.getType().getLabel());
        absence.setReason(trim(absence.getReason()));
        absence.setComment(trim(absence.getComment()));
    }

    private boolean overlaps(Absence left, Absence right) {
        LocalDate start = left.getStartDate().isAfter(right.getStartDate()) ? left.getStartDate() : right.getStartDate();
        LocalDate end = left.getEndDate().isBefore(right.getEndDate()) ? left.getEndDate() : right.getEndDate();
        if (end.isBefore(start)) return false;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (Boolean.TRUE.equals(left.getAllDay()) || Boolean.TRUE.equals(right.getAllDay())) return true;
            LocalTime leftStart = date.equals(left.getStartDate()) ? left.getStartHour() : LocalTime.MIN;
            LocalTime leftEnd = date.equals(left.getEndDate()) ? left.getEndHour() : LocalTime.MAX;
            LocalTime rightStart = date.equals(right.getStartDate()) ? right.getStartHour() : LocalTime.MIN;
            LocalTime rightEnd = date.equals(right.getEndDate()) ? right.getEndHour() : LocalTime.MAX;
            if (leftStart.isBefore(rightEnd) && leftEnd.isAfter(rightStart)) return true;
            date = date.plusDays(1);
        }
        return false;
    }

    private boolean overlaps(Absence absence, Planning planning) {
        return blocksPlanning(absence, planning.getDate(), planning.getStartHour(), planning.getEndHour());
    }

    private String trim(String value) { return value == null ? null : value.trim(); }
    private Map<String, String> error(String key, String value) { Map<String, String> errors = new HashMap<>(); errors.put(key, value); return errors; }
}
