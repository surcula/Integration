package be.atc.erpprojetintegration_1.security;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IAuthorizationService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.mappers.EmployeeMapper;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.inject.spi.CDI;
import java.util.List;

public class EmployeeRealm extends AuthorizingRealm {

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;

        String email = usernamePasswordToken.getUsername();
        String password = new String(usernamePasswordToken.getPassword());

        Result<Employee> employeeResult = getEmployeeService().getByEmail(email);

        if (employeeResult == null || !employeeResult.isSuccess() || employeeResult.getData() == null) {
            throw new UnknownAccountException("Employee not found");
        }

        Employee employee = employeeResult.getData();

        if (employee.getIsActive() == null || !employee.getIsActive()) {
            throw new DisabledAccountException("Employee account is disabled");
        }

        if (!BCrypt.checkpw(password, employee.getPassword())) {
            throw new IncorrectCredentialsException("Invalid password");
        }

        ConnectedEmployeeDto connectedEmployee = EmployeeMapper.toConnectedEmployeeDto(employee);

        return new SimpleAuthenticationInfo(
                connectedEmployee,
                usernamePasswordToken.getCredentials(),
                getName()
        );
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        ConnectedEmployeeDto employee = (ConnectedEmployeeDto) principals.getPrimaryPrincipal();

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        if (employee == null) {
            return authorizationInfo;
        }

        if (employee.getRoleName() != null) {
            authorizationInfo.addRole(employee.getRoleName());
        }

        if (employee.getRoleId() == null) {
            return authorizationInfo;
        }

        Result<List<String>> permissionsResult =
                getAuthorizationService().getAuthorizationNamesByRoleId(employee.getRoleId());

        if (permissionsResult != null
                && permissionsResult.isSuccess()
                && permissionsResult.getData() != null) {
            authorizationInfo.addStringPermissions(permissionsResult.getData());
        }

        return authorizationInfo;
    }

    private IEmployeeService getEmployeeService() {
        return CDI.current().select(IEmployeeService.class).get();
    }

    private IAuthorizationService getAuthorizationService() {
        return CDI.current().select(IAuthorizationService.class).get();
    }
}
