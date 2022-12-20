package phis.his.nu.logging;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/logging")
public class LoggingNuController {
    @GetMapping("/{instcd}")
    public ModelAndView getLog(@PathVariable String instcd) {
        ModelAndView mav = new ModelAndView("logSearch");
        mav.addObject("instcd", instcd);

        return mav;
    }
}
