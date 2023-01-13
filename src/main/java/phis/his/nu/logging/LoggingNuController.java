package phis.his.nu.logging;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/logging")
public class LoggingNuController {
    @GetMapping("/main")
    public ModelAndView main(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("main");
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        mav.addObject("ip_addr", ip);

        return mav;
    }

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

    // 상세 로그 열기
    @GetMapping("/ulog.nu")
    public ModelAndView getDetailLog(Logging logging, String instcd) throws Exception {
        ModelAndView mav = new ModelAndView("logDetail");
        String queryMessage = "ulog.nu?trid=" + logging.getTrid() +
                                      "&ctx=" + logging.getCtx()  +
                                     "&node=" + logging.getNode() +
                                     "&date=" + logging.getDate();

        Document doc = Jsoup.connect("http://emr" + instcd + "edu.cmcnu.or.kr?" + queryMessage).get();
        Elements preText = doc.getElementsByTag("pre");

        System.out.println(preText);
        System.out.println(preText.text());

        return mav;
    }
    
    // submit 보내기
    @GetMapping("/cmcnu/trlog.nu")
    public ModelAndView searchLog(Logging logging) throws Exception {
        String queryMessage = "ip_addr="   + logging.getIp_addr() +
                              "&svc_name=" + logging.getSvc_name() +
                              "&user_id="  + logging.getUser_id() +
                              "&tr_id="    + logging.getTr_id() +
                              "&date="     + logging.getDate() +
                              "&svc_url="  + logging.getSvc_url() +
                              "&succ_yn="  + logging.getSucc_yn() +
                              "&op_name="  + logging.getOp_name();

        Document doc = Jsoup.connect("http://emr" + logging.getInstcd() + "edu.cmcnu.or.kr?" + queryMessage).get();
        Elements table = doc.getElementsByTag("table");

        System.out.println("");

        ModelAndView mav = new ModelAndView("logSearch");
        mav.addObject("tableBody", table);
        mav.addObject("logging", logging);

        return mav;
    }
}
