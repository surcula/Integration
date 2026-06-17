package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines absence-related database operations.
 */
public interface IAbsenceService {

    /**
     * Retrieves all active absences regardless of employee.
     *
     * @return active absence list result
     */
    Result<List<Absence>> getAllActive();

    /**
     * Retrieves all active absences for a specific employee.
     *
     * @param employeeId employee id
     * @return active absence list result
     */
    Result<List<Absence>> getActiveByEmployee(Integer employeeId);

    /**
     * Retrieves a single absence by its identifier.
     *
     * @param id absence id
     * @return absence result
     */
    Result<Absence> getById(Integer id);

    /**
     * Creates or updates an absence record.
     *
     * @param absence absence to save
     * @return saved absence result
     */
    Result<Absence> save(Absence absence);

    /**
     * Retrieves approved absences for an employee that overlap the given date range,
     * optionally excluding one absence by id (used during edition).
     *
     * @param employeeId employee id
     * @param startDate  start of the range to check
     * @param endDate    end of the range to check
     * @param excludedId absence id to exclude, or null
     * @return overlapping absence list result
     */
    Result<List<Absence>> getBlockingAbsences(Integer employeeId, LocalDate startDate, LocalDate endDate, Integer excludedId);

    /**
     * Retrieves active planning entries assigned to an employee within a date range.
     * Used to detect conflicts before approving an absence.
     *
     * @param employeeId employee id
     * @param startDate  range start date
     * @param endDate    range end date
     * @return planning list result
     */
    Result<List<Planning>> getEmployeePlannings(Integer employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves pending sickness absences without a certificate whose start date
     * is before the given deadline. Used for automatic refusal.
     *
     * @param deadlineDate cutoff date
     * @return overdue sickness absence list result
     */
    Result<List<Absence>> getPendingSicknessWithoutCertificateBefore(LocalDate deadlineDate);
}
