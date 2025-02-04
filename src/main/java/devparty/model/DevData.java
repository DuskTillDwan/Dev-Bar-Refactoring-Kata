package devparty.model;

import java.time.LocalDate;
import java.util.List;

public class DevData {
    public String getName() {
        return name;
    }

    public List<LocalDate> getWorkingDaysIThink() {
        return workingDays;
    }

    private String name;
    private List<LocalDate> workingDays;

    public DevData(String name, List<LocalDate> workingDays) {
        this.name = name;
        this.workingDays = workingDays;
    }
}
