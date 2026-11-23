package ci.inphb.taskflow.model;

public enum Priority {
    BASSE("Basse"),
    MOYENNE("Moyenne"),
    HAUTE("Haute");

    private final String label;

    Priority(String label) { this.label = label; }

    public String getLabel() { return label; }
}
