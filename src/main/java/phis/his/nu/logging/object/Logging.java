package phis.his.nu.logging.object;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Logging {
    private String ip_addr;
    private String svc_name;
    private String user_id;
    private String tr_id;
    private String date;
    private String svc_url;
    private String succ_yn;
    private String op_name;

    private String trid;
    private String ctx;
    private String node;
    private String logdt;
    private String instcd;
    
    private String logUrl;
    private String submitIP;
}
