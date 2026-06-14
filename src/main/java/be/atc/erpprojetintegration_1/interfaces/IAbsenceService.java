package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

public interface IAbsenceService {
    Result<List<Absence>> getAllActive();
    Result<List<Absence>> getActiveByEmployee(Integer employeeId);
    Result<Absence> getById(Integer id);
    Result<Absence> save(Absence absence);
    Result<List<Absence>> getBlockingAbsences(Integer employeeId, LocalDate startDate, LocalDate endDate, Integer excludedId);
    Result<List<Planning>> getEmployeePlannings(Integer employeeId, LocalDate startDate, LocalDate endDate);
    Result<List<Absence>> getPendingSicknessWithoutCertificateBefore(LocalDate deadlineDate);
}
