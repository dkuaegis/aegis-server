package aegis.server.domain.timeTable.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import aegis.server.domain.timeTable.service.timeTableService;

import java.util.List;

@Controller
public class timeTableController {

    private final timeTableService timeTableService;

    @Autowired
    public timeTableController(timeTableService timeTableService){
        this.timeTableService = timeTableService;
    }

    @GetMapping("/crawlTimetable")
    public String crawlTimetable(Model model){
        List<String> timetableData = timeTableService.crawlTimetable();

        model.addAttribute("timetableData", timetableData);

        return "timetable";
    }

}
