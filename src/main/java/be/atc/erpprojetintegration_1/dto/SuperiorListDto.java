package be.atc.erpprojetintegration_1.dto;

public class SuperiorListDto {

    private Integer id;
    private Integer relationId;
    private Integer departmentHeadId;
    private Integer departmentId;
    private Integer superiorEmployeeId;
    private String employeeFullName;
    private String superiorFullName;
    private String departmentName;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private Integer managedEmployeesCount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRelationId() {
        return relationId;
    }

    public void setRelationId(Integer relationId) {
        this.relationId = relationId;
    }

    public Integer getDepartmentHeadId() {
        return departmentHeadId;
    }

    public void setDepartmentHeadId(Integer departmentHeadId) {
        this.departmentHeadId = departmentHeadId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getSuperiorEmployeeId() {
        return superiorEmployeeId;
    }

    public void setSuperiorEmployeeId(Integer superiorEmployeeId) {
        this.superiorEmployeeId = superiorEmployeeId;
    }

    public String getEmployeeFullName() {
        return employeeFullName;
    }

    public void setEmployeeFullName(String employeeFullName) {
        this.employeeFullName = employeeFullName;
    }

    public String getSuperiorFullName() {
        return superiorFullName;
    }

    public void setSuperiorFullName(String superiorFullName) {
        this.superiorFullName = superiorFullName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getManagedEmployeesCount() {
        return managedEmployeesCount;
    }

    public void setManagedEmployeesCount(Integer managedEmployeesCount) {
        this.managedEmployeesCount = managedEmployeesCount;
    }
}
