package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EvaluationBusiness;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class EvaluationsBean implements Serializable {

    private static final Logger log = Logger.getLogger(EvaluationsBean.class);

    @Inject
    private EvaluationBusiness evaluationBusiness;

    private List<Evaluation> evaluations;

    @PostConstruct
    public void init() {
        Result<List<Evaluation>> result = evaluationBusiness.getActiveEvaluations();

        if (result.isSuccess()) {
            evaluations = result.getData();
            log.info("Evaluations loaded in list page: " + evaluations.size());
        } else {
            evaluations = new ArrayList<>();
            log.warn("Evaluation list loading failed");
            MessageUtils.addErrorMessages(result, "evaluations.error.load");
        }
    }

    public String formatScore(BigDecimal score) {
        if (score == null) {
            return "-";
        }

        return score.stripTrailingZeros().toPlainString();
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }
}
