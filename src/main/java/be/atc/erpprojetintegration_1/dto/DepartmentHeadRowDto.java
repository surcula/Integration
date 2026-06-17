package be.atc.erpprojetintegration_1.dto;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;

import java.io.Serializable;

public class DepartmentHeadRowDto implements Serializable {
    private final Department department;
    private final DepartmentHead currentHead;
    private final int historyCount;

    public DepartmentHeadRowDto(Department department, DepartmentHead currentHead, int historyCount) {
        this.department = department;
        this.currentHead = currentHead;
        this.historyCount = historyCount;
    }

    public Department getDepartment() { return department; }
    public DepartmentHead getCurrentHead() { return currentHead; }
    public int getHistoryCount() { return historyCount; }
    public boolean isHasCurrentHead() { return currentHead != null; }
    public boolean isHasHistory() { return historyCount > 0; }
}
