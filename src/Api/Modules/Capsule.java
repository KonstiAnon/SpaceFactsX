package Api.Modules;

public record Capsule(int reuseCount, int waterLandings, int landLandings, String lastUpdate, String[] launches,
                      String serial, Status status, String type, String id) {

    public String toString(){
        return "null";
    }
}

enum Status {
    retired, active;
}