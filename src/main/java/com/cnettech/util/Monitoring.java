package com.cnettech.util;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sun.management.OperatingSystemMXBean;

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
                if (SystemCode.equals("")) return;
                int Cpu = (int)Math.round(getCpuUsage());
                int Mem = (int)Math.round(getMemoryUsage());
                String DiskStr = "";
                String Disks[] = pros.getProperty("CHECK_ALARM_DISK").split(",");
                for (String disk : Disks) {
//                    Map<String, Integer> tempDisk = new HashMap<>();
                    int DiskUsage = 0;
                    if (SystemType.contains("Windows")) {
                        DiskUsage =(int)Math.round(getDiskUsage(disk + ":"));
                    } else {
                        DiskUsage = (int)Math.round(getDiskUsage(disk));
                    }
                    DiskStr += (DiskStr.length() > 0 ? "," : "" ) + disk + ":" + String.format("%d",DiskUsage);
                }

                int ProcessCnt = Integer.parseInt(pros.getProperty("PROCESS_COUNT"));
                String ProcessStr = "";
                
                for (int i = 1; i <= ProcessCnt; i++) {
                    if (SystemType.contains("Windows")) {
                        // 윈도우용
                        ProcessStr += getProcInfo(pros.getProperty("PROGRAM_" + String.valueOf(i)));
                    } else {
                        // 리눅스용
                        ProcessStr += findProcess(pros.getProperty("PROGRAM_" + String.valueOf(i)));
                    }
                }
                //System.out.printf(", HDD : %s", DiskStr);
                
                //Map<String, String> Disk = (int)Math.round(getDiskUsage("c:"));
                // System.out.printf("CPU : %d", Cpu);
                // System.out.printf(", MEM : %d", Mem);
                // System.out.printf(", HDD : %d", );
                System.out.printf("CPU : %s, MEM : %s, HDD : %s, Process : %s\r\n", String.valueOf(Cpu), String.valueOf(Mem), DiskStr, ProcessStr);
                //UdpClient.SendMsg("127.0.0.1",4001,"test");
                SendURL();

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        return osBean.getSystemCpuLoad() * 100;
    }
    
    private double getMemoryUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double Free_Memory_Size = osBean.getFreePhysicalMemorySize();
        double Total_Memory_Size = osBean.getTotalPhysicalMemorySize();
        return (100 - ((Free_Memory_Size / Total_Memory_Size) * 100));
    }
    
    private double getDiskUsage(String DriveName) {
        System.out.println("DriveName = " + DriveName);
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
                    e.printStackTrace();
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

    public void SendURL()
    {
        try {
            URL url = new URL("http://192.168.0.115:8888/monitoring/check");
            String query = "{\"cpu\":0,\"hdd\":0,\"mem\":0,\"systemcode\":\"SystemCode\"}";
            //It change the apostrophe char to double quote char, to form a correct JSON string
            query=query.replace("'", "\"");
            URLConnection urlc = url.openConnection();
            //It Content Type is so important to support JSON call
            urlc.setRequestProperty("Content-Type", "application/json");
            System.out.println("Connection: " + url.toString());
            //use post mode
            urlc.setDoOutput(true);
            urlc.setAllowUserInteraction(false);

            //send query
            PrintStream ps = new PrintStream(urlc.getOutputStream());
            ps.print(query);
            System.out.println("String: " + query);
            ps.close();

            //get result
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String l = null;
            while ((l=br.readLine())!=null) {
                System.out.printf("String: " + l);
            }
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
