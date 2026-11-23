package ci.inphb.taskflow.model;

public enum Status {
    A_FAIRE("À faire"),
    EN_COURS("En cours"),
    TERMINEE("Terminée");

    private final String label;

    Status(String label) { this.label = label; }

    public String getLabel() { return label; }
}
