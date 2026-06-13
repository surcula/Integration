package be.atc.erpprojetintegration_1.enums;

public enum AbsenceType {
    SICKNESS("Maladie"),
    WORK_ACCIDENT("Accident de travail"),
    ANNUAL_LEAVE("Conge annuel"),
    UNPAID_LEAVE("Conge sans solde"),
    MATERNITY_PATERNITY("Maternite / paternite"),
    MEDICAL_APPOINTMENT("Rendez-vous medical"),
    TRAINING("Formation"),
    MISSION("Mission"),
    JUSTIFIED("Absence justifiee"),
    UNJUSTIFIED("Absence injustifiee"),
    OTHER("Autre");

    private final String label;

    AbsenceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
