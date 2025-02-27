package devparty.model;

import java.time.LocalDate;
import java.util.List;

public record Dev(String Name, List<LocalDate> onSite){
}
