package com.oversea.task;

import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Properties;

public class Launcher {

    private static String clientJarZipName = "taskClient-distribution.zip";

    private static String clientJarName = "taskClient.jar";

    //爬虫
//    private static String downloadJarLink = "http://122.225.114.22:50256/upgrade_jar";
    
//    private static String downloadJarLink = "http://122.225.114.19:30010/upgrade_jar";
    
    private static String downloadJarLink = "http://122.225.114.28:8050/upgrade_jar";

    //测试
//    private static String downloadJarLink = "http://122.225.114.20:3401/upgrade_jar";

    //下单
    //private static String downloadJarLink = "http://122.225.114.24:50256/upgrade_jar";

    private static String startJarBat = "startup.bat";

    private static final String startWinShellCmd = "cmd /c start ";
    
    private static String startJarSh = "startup.sh";

    private static final String startLinuxShellCmd = "/bin/sh ";

    private static final String[] findWinTaskClientJarCmd = {"cmd", "/c", "tasklist -v /fo csv |findstr taskClient"};

    private static final String killWinTaskClientJarCmd = "cmd /c taskkill /f /t /PID ";

    private static final String LOG_FILE = "shell_log.log";

    private static LogFile logFile = null;

    public static void main(String[] args) throws IOException {
    	boolean isLinux = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 ? false : true;
    	String workHome = getProjectPath();
    	logFile = new LogFile(workHome + File.separator + "log" + File.separator + LOG_FILE);
         
    	String dlUrl = downloadJarLink;
    	if(args != null && args.length > 0){
    		dlUrl = args[0];
    		logFile.append("-1.获取传递参数链接 " + dlUrl+ "." );
    	}
        logFile.append("0.工作目录:" + workHome);
        logFile.append("1.开始运行shell升级程序. ");
        logFile.append("2.开始检查 " + clientJarName + ".");
        logFile.append("2.链接下载路径 " + dlUrl + ".");

        //杀死正在执行的jar包
        logFile.append("2.5.杀死正在执行的任务");
        if(isLinux) {
        	linuxKillProcess("taskClient");
        } else {
        	killProcess("taskClient");
        }

        //删除zip包
        logFile.append("3.开始检查老旧 " + clientJarZipName);
        File zipFile = new File(workHome + File.separator + clientJarZipName);
        if (zipFile.exists()) {
            logFile.append("3.1.删除老旧 " + clientJarZipName + ":" + zipFile.delete());
        }

        //下载文件
        logFile.append("4.开始下载新的 " + clientJarZipName);
        zipFile = new File(workHome + File.separator + clientJarZipName);
        FileOutputStream fos = null;
        try {
            byte[] result = null;
            while (true) {
                result = HttpClient.httpGet(dlUrl+"?r="+Math.random());
                if (result != null) {
                    break;
                }
                logFile.append("***********下载zip机器暂时不可用,等4秒重试***************");
                Thread.sleep(4000L);
            }
            fos = new FileOutputStream(zipFile);
            fos.write(result, 0, result.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //解压
        logFile.append("5.开始解压新的 " + clientJarZipName);
        ZipUtil.unZipFiles(workHome + File.separator + clientJarZipName, workHome + File.separator);

        logFile.append("6.开始启动,执行脚本");
        //启动jar
        try {
        	Process p = null;
        	if(isLinux) {
        		p = Runtime.getRuntime().exec(startLinuxShellCmd + startJarSh);
        	} else {
        		p = Runtime.getRuntime().exec(startWinShellCmd + startJarBat);
        	}
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //删除zip包
        logFile.append("7.开始删除解压包 " + clientJarZipName);
        zipFile = new File(workHome + File.separator + clientJarZipName);
        if (zipFile.exists()) {
            logFile.append("7.1.删除老旧 " + clientJarZipName + ":" + zipFile.delete());
        }
        logFile.append("8.结束~~~");
        logFile.append("************************结束了*****************************");
        if(isLinux) {
        	linuxKillProcess("client-shell");
        } else {
        	killProcess("taskShell");
        }
        System.exit(0);
    }

    public static String getProjectPath() {
        URL url = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
        String filePath = url.getPath();
        if (filePath.endsWith(".jar")) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }
        File file = new File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }

    /**
     * 加载配置
     *
     * @return
     */
    public static Properties loadProperties() {
        File file = new File("config" + File.separator + "system-shell-default.properties");
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
        } catch (FileNotFoundException e) {
            System.out.print("请把配置文件:system-shell-default.properties 放在jar包路径config目录下~:" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException ignored) {
            }
        }
        return properties;
    }
    
    public static void killProcess(String processName)
	{

		try
		{
			Process p = Runtime.getRuntime().exec(new String[] { "cmd", "/c", "tasklist -v /fo csv |findstr " + processName });
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(new InputStreamReader((p.getInputStream()), "GBK"));
				String line = null;
				while ((line = br.readLine()) != null)
				{
					String[] params = line.split(",");
					String pid = params[1].replace("\"", "");
					logFile.append("find " + processName + " with pid:" + pid);
					p = Runtime.getRuntime().exec(new String[] { "cmd", "/c", "taskkill /f /t /pid " + pid });
					p.waitFor();
					logFile.append("kill pid:" + pid);
				}
			}
			finally
			{
				if (br != null)
				{
					br.close();
				}
			}
		}
		catch (Exception e)
		{
			logFile.append("kill the " + processName + " meet error:" + e.getMessage());
		}
	}
    
    public static void linuxKillProcess(String processName) {
    	try {
    		String[] cmd = new String[] {"/bin/sh", "-c", "ps x | grep " + processName + " | grep -v grep | awk  '{print $1}'"};
			Process ps = Runtime.getRuntime().exec(cmd);
			ps.waitFor();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line = null;
			while((line=br.readLine()) != null) {
				String pid = line.trim();
				logFile.append("find " + processName + " with pid:" + pid);
				ps = Runtime.getRuntime().exec("kill -15 " + pid);
				ps.waitFor();
				logFile.append("kill pid:" + pid);
			}
			
			if(br != null) br.close();
		} catch (Exception e) {
			e.printStackTrace();
			logFile.append("kill the " + processName + " meet error:" + e.getMessage());
		}
    }

    /**
     * 杀死客户端jar进程
     */
    public static void killTaskClientJarProcessor() {
        try {
            Process p = Runtime.getRuntime().exec(findWinTaskClientJarCmd);
            BufferedReader buffer = null;
            try {
                buffer = new BufferedReader(new InputStreamReader((p.getInputStream())));
                String temp = null;
                String pid = null;
                while ((temp = buffer.readLine()) != null) {
                    String[] segs = temp.split(",");
                    pid = segs[1].replace("\"", "");
                    logFile.append("找到" + clientJarName + "进程pid:" + pid + ".");
                    p = Runtime.getRuntime().exec(killWinTaskClientJarCmd + pid);
                    p.waitFor();
                    logFile.append("杀死进程pid:" + pid);
                    break;
                }
                if (pid == null) {
                    logFile.append(clientJarName + "进程已经结束");
                }
            } finally {
                if (buffer != null) {
                    buffer.close();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
