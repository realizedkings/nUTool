package phis.his.nu.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import phis.his.nu.logging.object.Logging;

@Controller
@RequestMapping("/logging")
public class LoggingNuController {
    private LoggingNuService loggingNuService;
    private Logger log = LoggerFactory.getLogger(phis.his.nu.logging.LoggingNuController.class);	
    
    @Autowired
    public void setLoggingNuService(LoggingNuService loggingNuService) {
        this.loggingNuService = loggingNuService;
    }

    @GetMapping("/main")
    public ModelAndView main(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("main");
        mav.addObject("ip_addr", getIP(request));

        return mav;
    }
    
    private String getIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null)
          ip = request.getHeader("Proxy-Client-IP"); 
        if (ip == null)
          ip = request.getHeader("WL-Proxy-Client-IP"); 
        if (ip == null)
          ip = request.getHeader("HTTP_CLIENT_IP"); 
        if (ip == null)
          ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        if (ip == null)
          ip = request.getRemoteAddr(); 
        
        return ip;
      }
    
    // 상세 로그 열기
    @GetMapping("/cmcnu/ulog.nu")
    public ModelAndView getDetailLog(Logging logging, HttpServletRequest request) throws Exception {
        ModelAndView mav = new ModelAndView("logDetail");
        String queryMessage = "ulog.nu?trid=" + logging.getTrid() +
                                      "&ctx=" + logging.getCtx()  +
                                     "&node=" + logging.getNode() +
                                     "&date=" + logging.getDate();
        
        String detailLogURL = "http://emr" + logging.getInstcd() + "edu.cmcnu.or.kr/cmcnu/" + queryMessage;
        logging.setLogUrl(detailLogURL);
        logging.setSubmitIp(getIP(request));
        try {
        	Document doc = Jsoup.connect(detailLogURL).timeout(1000).get();
        	doc.outputSettings().prettyPrint(false);
        	Elements preText = doc.getElementsByTag("body");
        	
        	List<Map<String, String>> logs = null;
        	
            logs = loggingNuService.parseLog(preText.html());
            
            mav.addObject("logs", logs);
            mav.addObject("logging", logging);
            mav.addObject("originalUrl", "http://emr" + logging.getInstcd() + "edu.cmcnu.or.kr/cmcnu/" + queryMessage);
        } catch (Exception e) {
            mav.setViewName("redirect:" + detailLogURL);
            log.debug(" REDIRECT ERROR : " + detailLogURL);
            log.debug(e.getMessage());
        }
        
        loggingNuService.insertDetailLogHistory(logging);
        
        return mav;
    }
    
    // submit 보내기
    @GetMapping("/cmcnu/trlog.nu")
    public ModelAndView searchLog(Logging logging, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (logging.getDate() == null
                        || "".equals(logging.getDate())) {    // 처음 페이지 오픈 시 날짜 지정
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
            String date = now.format(dateTimeFormatter);
            logging.setDate(date);
        }

        String queryMessage = "ip_addr="   + logging.getIp_addr() +
                              "&svc_name=" + logging.getSvc_name() +
                              "&user_id="  + logging.getUser_id() +
                              "&tr_id="    + logging.getTr_id() +
                              "&date="     + logging.getDate() +
                              "&svc_url="  + logging.getSvc_url() +
                              "&succ_yn="  + logging.getSucc_yn() +
                              "&op_name="  + logging.getOp_name();

        Elements table = null;
        int connectCount = 0;

        while (connectCount < 2) {
            try {
                Document doc = Jsoup.connect("http://emr" + logging.getInstcd() + "edu.cmcnu.or.kr/cmcnu/trlog.nu?" + queryMessage).get();
                table = doc.getElementsByTag("table");
                break;
            } catch (HttpStatusException exception) {
            	this.log.debug(exception.getMessage());
            	connectCount++;
            }
        }
        
        Cookie[] cookies = request.getCookies();
        Cookie submitCookie = null;
        boolean submitTimeFlag = false;
        for (Cookie cookie : cookies) {
          if ("submitTime".equals(cookie.getName()))
            submitTimeFlag = true; 
        } 
        if (!submitTimeFlag) {
          logging.setSubmitIp(getIP(request));
          this.loggingNuService.insertSubmitHistory(logging);
          submitCookie = new Cookie("submitTime", "Y");
          submitCookie.setMaxAge(60);
          response.addCookie(submitCookie);
        } 
        
        ModelAndView mav = new ModelAndView("logSearch");
        mav.addObject("tableBody", table.html());
        mav.addObject("logging", logging);
        
        return mav;
    }
}
