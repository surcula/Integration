package be.atc.erpprojetintegration_1.dto;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;

import java.io.Serializable;

public class EmployeeDepartmentRowDto implements Serializable {
    private final EmployeeDepartment assignment;
    private final int historyCount;

    public EmployeeDepartmentRowDto(EmployeeDepartment assignment, int historyCount) {
        this.assignment = assignment;
        this.historyCount = historyCount;
    }

    public EmployeeDepartment getAssignment() {
        return assignment;
    }

    public int getHistoryCount() {
        return historyCount;
    }

    public boolean isHasHistory() {
        return historyCount > 0;
    }
}
