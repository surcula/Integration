package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ProfileBean {

    private static final Logger log = Logger.getLogger(ProfileBean.class);

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;
    private EmployeeProfileDto profil;

    /**
     * Loads the connected employee profile when the page is initialized.
     */
    @PostConstruct
    public void init() {
        Integer employeeId = authBean.getConnectedEmployee().getId();
        log.info("Loading profile for employee id: " + employeeId);

        Result<EmployeeProfileDto> profileResult = employeeBusiness.getProfile(employeeId);
        if(profileResult.isSuccess()){
            profil = profileResult.getData();
            log.info("Profile loaded for employee id: " + employeeId);
        }else {
            log.warn("Profile loading failed for employee id: " + employeeId);
            MessageUtils.addErrorMessages(profileResult, "profile.error.load");
        }
    }

    /**
     * Returns the connected employee profile displayed by the profile page.
     *
     * @return employee profile dto
     */
    public EmployeeProfileDto getEmployeeProfileDto() {
        return profil;
    }

}
