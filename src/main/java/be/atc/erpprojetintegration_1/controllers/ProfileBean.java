package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ProfileBean {

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;
    private EmployeeProfileDto profil;

    @PostConstruct
    public void init() {
        Integer employeeId = authBean.getConnectedEmployee().getId();
        Result<EmployeeProfileDto> profileResult = employeeBusiness.getProfile(employeeId);
        if(profileResult.isSuccess()){
            profil = profileResult.getData();
        }else {
            MessageUtils.addErrorMessages(profileResult, "profile.error.load");
        }
    }

    public EmployeeProfileDto getEmployeeProfileDto() {
        return profil;
    }

}