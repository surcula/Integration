package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Contract;
import be.atc.erpprojetintegration_1.tools.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IContractService {
    Result<List<Contract>> getAll();
    Result<List<Contract>> getByEmployee(Integer employeeId);
    Result<Contract> getActive(Integer employeeId);
    Result<Contract> getById(Integer contractId);
    Result<Contract> create(Contract contract, Integer employeeId);
    Result<Void> close(Integer contractId, LocalDate endDate);
    Result<Void> renew(Integer contractId, LocalDate newEndDate);
    Result<Void> updateSalary(Integer contractId, BigDecimal newSalary);
}
