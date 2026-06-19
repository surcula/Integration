package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IEmployeeService {

    /**
     * Retrieves an employee by their unique identifier.
     *
     * @param id the unique identifier of the employee
     * @return a Result containing the employee if found, or an error if no employee matches the given id
     */
    Result<Employee> getById(Integer id);

    /**
     * Retrieves an employee by their email address.
     *
     * @param email the email address used to search for the employee
     * @return a Result containing the employee if found, or an error if no employee matches the given email
     */
    Result<Employee> getByEmail(String email);

    /**
     * Retrieves all active employees.
     *
     * @return a Result containing the list of active employees, or an error if the operation fails
     */
    Result<List<Employee>> getAllActive();

    /**
     * Retrieves all active employees with their department relation when one exists.
     *
     * @return a Result containing the list of active employees with department data,
     *         or an error if the operation fails
     */
    Result<List<Employee>> getAllActiveWithDepartments();

    /**
     * Retrieves all employees with their department relation when one exists.
     *
     * @return a Result containing the list of employees with department data,
     *         or an error if the operation fails
     */
    Result<List<Employee>> getAllWithDepartments();

    /**
     * Retrieves all employees, including inactive ones.
     *
     * @return a Result containing the list of employees, or an error if the operation fails
     */
    Result<List<Employee>> getAll();

    /**
     * Creates a new employee.
     *
     * @param employee the employee entity to persist
     * @return a Result containing the created employee if the operation succeeds,
     *         or validation/persistence errors if it fails
     */
    Result<Employee> create(Employee employee);

    /**
     * Updates an existing employee.
     *
     * @param employee the employee entity containing the updated data
     * @return a Result containing the updated employee if the operation succeeds,
     *         or an error if the update fails
     */
    Result<Employee> update(Employee employee);

    /**
     * Replaces an employee password with a temporary hashed password.
     *
     * @param id employee identifier
     * @param hashedPassword BCrypt hashed temporary password
     * @return a successful result when the password is reset
     */
    Result<Void> resetPassword(Integer id, String hashedPassword);

    /**
     * Deactivates an employee without deleting them from the database.
     *
     * @param id the unique identifier of the employee to deactivate
     * @return a successful Result if the employee is deactivated,
     *         or an error if no employee matches the given id or if the operation fails
     */
    Result<Void> deactivate(Integer id);

    /**
     * Reactivates an inactive employee.
     *
     * @param id the unique identifier of the employee to activate
     * @return a successful Result if the employee is activated,
     *         or an error if no employee matches the given id or if the operation fails
     */
    Result<Void> activate(Integer id);


}
