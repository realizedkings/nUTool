package phis.his.nu.logging;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@Service
public class LoggingNuServiceImpl implements LoggingNuService {

    @Override
    public String getLogging(String log) {
        return null;
    }

    @Override
    public List<Map<String, String>> parseLog(String log) {
        return null;
    }

    @Override
    public Map<String, String> makeQuery(String log) {
        return null;
    }

    public static void main(String[] args) throws Exception {
        File file = null;
        FileInputStream fileInputStream = null;
        StringBuilder text = new StringBuilder();

        try {
            file = new File("C:\\Users\\user\\OneDrive\\자료\\PARAM\\정상 종료 case.txt");
            fileInputStream = new FileInputStream(file);

            int next = 0;
            while ((next = fileInputStream.read()) != -1) {
                text.append((char) next);
            }

        } catch (Exception e) {

        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        parsingTest(text.toString());
    }

    private static void parsingTest(String text) {
        String[] allLines = text.split("\n");
        ArrayList<HashMap<String, String>> exitMethod = new ArrayList<>();
        int layer = 0;

        for (int i = 0; i < allLines.length; i++) {
            String line = allLines[i];

            // 일반 로그
            if (line.indexOf("[") == 9 && line.indexOf("node=") == 10) {
                int start = 0;
                int end   = 0;

                start = line.indexOf("[", start);
                end   = line.indexOf("]", end);
                String nodeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String timeInfo = line.substring(start + 1, end);

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String printInfo = line.substring(start + 1, end).trim();

                start = line.indexOf("[", start + 1);
                end   = line.indexOf("]", end + 1);
                String packageInfo = line.substring(start + 1, end);

                if (line.indexOf("starts.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("starts.", end));

                    HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName + "starts.");
                    methodInfo.put("startTime", timeInfo);
                    methodInfo.put("package", packageInfo);
                    methodInfo.put("layer", layer + "");

                    exitMethod.add(methodInfo);

                    layer++;
                }

                if (line.indexOf("ends.", end) != -1) {
                    String methodName = line.substring(end + 1, line.indexOf("ends.", end));
                    String runTime = line.substring(line.lastIndexOf("(") + 1);
                    System.out.println(line);
                    HashMap<String, String> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", methodName + "ends. " + runTime);
                    methodInfo.put("endTime", timeInfo);
                    methodInfo.put("runTime", runTime);
                    methodInfo.put("package", packageInfo);
                    methodInfo.put("layer", (layer - 1) + "");

                    exitMethod.add(methodInfo);

                    layer--;
                }
            }
        }

        for (int i = 0; i < exitMethod.size(); i++) {
            Map<String, String> printMethod = exitMethod.get(i);
            StringBuilder tabText = new StringBuilder();
            StringBuilder packageInfo = new StringBuilder(printMethod.get("package"));

            for (int k = 0; k < 100 - packageInfo.length(); k++) packageInfo.append(" ");


            for (int j = 0; j < Integer.parseInt(printMethod.get("layer")); j++) {
                tabText.append("  ");
            }

            System.out.print(packageInfo.toString());
            System.out.println(tabText.toString() + printMethod.get("methodName"));
        }
    }
}
