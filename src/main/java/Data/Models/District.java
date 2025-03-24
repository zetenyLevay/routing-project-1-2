package Data.Models;

public class District {
    private int id;
    private String name;
    private double wealthRank;

    public District(int id, String name, double wealthRank) {
        this.id = id;
        this.name = name;
        this.wealthRank = wealthRank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWealthRank() {
        return wealthRank;
    }

    public void setWealthRank(double wealthRank) {
        this.wealthRank = wealthRank;
    }
}
