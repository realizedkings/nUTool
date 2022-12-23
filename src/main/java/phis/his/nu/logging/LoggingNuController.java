package phis.his.nu.logging;

import lombok.extern.java.Log;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/logging")
public class LoggingNuController {
    @GetMapping("/{instcd}")    // 기관별 교육 로그 페이지
    public ModelAndView getLogPage(@PathVariable String instcd, Logging logging) {
        ModelAndView mav = new ModelAndView("logSearch");

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String date = now.format(dateTimeFormatter);
        logging.setDate(date);

        mav.addObject("instcd", instcd);
        mav.addObject("logging", logging);

        return mav;
    }

    // 로그 열기
    @GetMapping("/ulog.nu")
    public ModelAndView getDetailLog(Logging logging) {
        return null;
    }
    
    // submit 보내기
    @GetMapping("/cmcnu/trlog.nu")
    public ModelAndView searchLog(Logging logging) {
        return null;
    }
}
