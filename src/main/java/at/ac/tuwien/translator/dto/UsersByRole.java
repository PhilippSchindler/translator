package at.ac.tuwien.translator.dto;

public class UsersByRole {

    private int developers;
    private int translators;
    private int releaseManagers;

    public UsersByRole(int developers, int translators, int releaseManagers) {
        this.developers = developers;
        this.translators = translators;
        this.releaseManagers = releaseManagers;
    }

    public int getDevelopers() {
        return developers;
    }

    public void setDevelopers(int developers) {
        this.developers = developers;
    }

    public int getTranslators() {
        return translators;
    }

    public void setTranslators(int translators) {
        this.translators = translators;
    }

    public int getReleaseManagers() {
        return releaseManagers;
    }

    public void setReleaseManagers(int releaseManagers) {
        this.releaseManagers = releaseManagers;
    }
}
