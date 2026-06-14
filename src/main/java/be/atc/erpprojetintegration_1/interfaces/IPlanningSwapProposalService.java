package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.PlanningSwapProposal;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IPlanningSwapProposalService {
    Result<List<PlanningSwapProposal>> getByEmployee(Integer employeeId);
    Result<List<PlanningSwapProposal>> getByRequest(Integer requestId);
    Result<Void> propose(Integer requestId, List<Integer> employeeIds, Integer proposedByEmployeeId, String comment);
    Result<Void> accept(Integer proposalId, Integer employeeId);
    Result<Void> decline(Integer proposalId, Integer employeeId);
    Result<Void> expireOverdue();
}
