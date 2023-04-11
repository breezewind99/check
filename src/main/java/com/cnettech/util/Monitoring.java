package com.cnettech.util;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Monitoring extends Thread {
    private boolean running;
    private Properties pros = Common.getProperties();
    String SystemType = System.getProperty("os.name");
    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                String SystemCode = pros.getProperty("SYSTEM_CODE");
                String SystemName = pros.getProperty("SYSTEM_NAME");
                String Alarm_Url[] = pros.getProperty("ALARM_URL").split(",");
                String Process[] = pros.getProperty("PROCESS").split(",");
                int Alarm_Sleep = Integer.parseInt(pros.getProperty("ALARM_SLEEP"));
                if (SystemCode.equals("")) return;
                int Cpu = 0;
                int Mem = 0;
                if (SystemType.contains("Windows")) {
                    Cpu = (int)Math.round(getCpuUsage());
                    Mem = (int)Math.round(getMemoryUsage());
                } else {
                    Cpu = (int)Math.round(getCpuUsage_Linux());
                    Mem = (int)getMemoryUsage_Linux();
                }

                String DiskStr = "";
                String Disks[] = pros.getProperty("CHECK_ALARM_DISK").split(",");
                for (String disk : Disks) {
                    int DiskUsage = 0;
                    if (SystemType.contains("Windows")) {
                        DiskUsage =(int)Math.round(getDiskUsage(disk + ":"));
                    } else {
                        DiskUsage = (int)Math.round(getDiskUsage(disk));
                    }
                    DiskStr += (DiskStr.equals("") ? "" : ",") + disk + ":" + String.format("%d",DiskUsage);
                }

                String ProcessStr = "";
                for (String process : Process) {
                    if (SystemType.contains("Windows")) {
                        // 윈도우용
                        ProcessStr += (ProcessStr.equals("") ? "" : ",") + getProcInfo(process);
                    } else {
                        // 리눅스용
//                        System.out.printf("CPU : %s\r\n", process);
                        ProcessStr += (ProcessStr.equals("") ? "" : ",") + findProcess(process);
                    }
                }

                System.out.printf("CPU : %s, MEM : %s, HDD : %s, Process : %s\r\n", String.valueOf(Cpu), String.valueOf(Mem), DiskStr, ProcessStr);

                for (String alarm : Alarm_Url) {
                    SendURL(alarm, SystemName, SystemCode, String.valueOf(Cpu), String.valueOf(Mem), DiskStr, ProcessStr);
                }

                Thread.sleep(Alarm_Sleep);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return osBean.getSystemCpuLoad() * 1000;
    }

    private double getCpuUsage_Linux() {
        double cpuUsage = 0;
        try {
            Process process = Runtime.getRuntime().exec("top -b -n 1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("%Cpu(s):")) {
                    System.out.println("line: " + line);
                    String[] cpuUsageInfo = line.split("\\s+");
                    cpuUsage = Double.parseDouble(cpuUsageInfo[1].replace(",", ""));
//                    System.out.println("CPU Usage: " + cpuUsage + "%");
                    break;
                }
            }

            reader.close();
            process.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cpuUsage;
    }
    
    private double getMemoryUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double Free_Memory_Size = osBean.getFreePhysicalMemorySize();
        double Total_Memory_Size = osBean.getTotalPhysicalMemorySize();
        return (100 - ((Free_Memory_Size / Total_Memory_Size) * 100));
    }

    private double getMemoryUsage_Linux() {
        double availMem=0;
        double totalMem=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/meminfo"));
            String line;
            while ((line = br.readLine()) != null) {
                //MemTotal
                if (line.startsWith("MemTotal:")) {
                    String[] tokens = line.split("\\s+");
                    totalMem = Double.parseDouble(tokens[1]) / 1024;
                }
                if (line.startsWith("MemAvailable:")) {
                    String[] tokens = line.split("\\s+");
                    availMem = Double.parseDouble(tokens[1]) / 1024;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double Used = totalMem - availMem;
//        System.out.println("Total memory: " + totalMem + " kB");
//        System.out.println("Available memory: " + availMem + " kB");
//        System.out.println("Used memory: " + Used + " kB");
        return (100-((availMem / totalMem) * 100));
    }
    
    private double getDiskUsage(String DriveName) {
//        System.out.println("DriveName = " + DriveName);
        File root = null;
        try {
            root = new File(DriveName);
            double Total_Disk_Size = toMB(root.getTotalSpace());
            double Free_Disk_Size = toMB(root.getUsableSpace());
            return (100 - ((Free_Disk_Size / Total_Disk_Size) * 100));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int toMB(long size) {
        return (int) (size / (1024 * 1024));
    }

    private String getProcInfo(String name) {
        try {
            String ReturnValue = "";
            String line;
            Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                // System.out.println(line); //<-- Parse data here.
                String[] words = line.split(" ");
                String[] procInfo = new String[10];
                if (words[0].equalsIgnoreCase(name)) {
                    // System.out.println(line);
                    int nCnt = 0;
                    for (String item : words) {
                        if (item.equals(""))
                            continue;
                        // System.out.print(item + " ");
                        procInfo[nCnt] = item;
                        nCnt++;
                    }
                    // System.out.println("Process Name : " + procInfo[0]);
                    // System.out.println("Process ID : " + procInfo[1]);
                    // System.out.println("Memory Usage : " + procInfo[4]);
                    ReturnValue = words[0];
                    break;
                }

            }
            input.close();
            ReturnValue = (ReturnValue.length() > 0 ? "O" + name : "X" + name);
            return ReturnValue;
        } catch (Exception err) {
            err.printStackTrace();
            return "X" + name;
        }
    }

    public String  findProcess(String processName) {
        String filePath = new String("");
        File directory = new File("/proc");
        File[] contents = directory.listFiles();
        boolean found = false;

        for (File f : contents) {
            if (f.getAbsolutePath().matches("\\/proc\\/\\d+")) {
                filePath = f.getAbsolutePath().concat("/status");
                try {
                    if (readFile(filePath, processName))
                        found = true;
                } catch (IOException e) {
                }
            }
        }
        if (found) {
            return "O" + processName;
        } else {
            return "X" + processName;
        }
    }

    public boolean readFile(String filename, String processName)
            throws IOException {
        FileInputStream fstream = new FileInputStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        strLine = br.readLine().split(":")[1].trim();
        br.close();
        if (strLine.equals(processName))
            return true;
        else
            return false;
    }

    public void SendURL(String CheckUrl, String SystemName, String SystemCode, String CPU, String MEM, String HDD, String Process)
    {
        try {

            URL url = new URL(CheckUrl);

            String JsonData = "{'CPU':"+ CPU +",'HDD':'" + HDD + "','MEM':" + MEM + ",'PROCESS':'"+ Process + "','SystemName':'" + SystemName +"','SystemCode':'" + SystemCode + "'}";
            JsonData=JsonData.replace("'", "\"");
            System.out.println("JsonData: " + JsonData);

            URLConnection urlc = url.openConnection();
            urlc.setRequestProperty("Content-Type", "application/json");
            System.out.println("Connection: " + url.toString());
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(JsonData);

            ps.close();

            //get result
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String l = null;
            while ((l=br.readLine())!=null) {
                System.out.println("String: " + l);
            }
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
