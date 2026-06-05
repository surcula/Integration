package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IEmployeeService {
    Result<Employee> login(String email, String password);

    Result<Employee> getById(Integer id);

    Result<Employee> getByEmail(String email);

    Result<List<Employee>> getAllActive();

    Result<List<Employee>> getAll();

    Result<Employee> create(Employee employee);

    Result<Employee> update(Employee employee);

    Result<Void> deactivate(Integer id);
}
