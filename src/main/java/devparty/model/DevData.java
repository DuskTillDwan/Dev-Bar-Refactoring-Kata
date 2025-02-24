package devparty.model;

import java.time.LocalDate;
import java.util.List;

public record DevData(String name, List<LocalDate> onSite) {

}
