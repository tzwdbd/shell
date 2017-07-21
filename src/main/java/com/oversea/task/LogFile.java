package com.oversea.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.oversea.task
 * @Description: 将内容写入文件(替代log4j)
 * @date 15/12/10 18:37
 */
public class LogFile {

    protected String filename;

    protected File file;

    protected FileWriter fWriter;

    protected PrintWriter pWriter;

    public LogFile(String filename) throws IOException {
        this.filename = filename;
        file = new File(filename);
    }

    public synchronized void append(String log) {
        fWriter = null;
        pWriter = null;
        try {
            fWriter = new FileWriter(file, true);
            pWriter = new PrintWriter(fWriter);
            // append to log file 加入时间
            DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pWriter.println("[" + ymdhmsFormat.format(new Date()) + "]" + log);
            // output it to console
            System.out.println(log);
        } catch (IOException e) {
            System.out.println("Error Writing log file: " + e);
        } finally {
            try {
                if (pWriter != null) {
                    pWriter.close();
                }
                if (fWriter != null) {
                    fWriter.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Return the length of the log file
     *
     * @return the length of the log file
     */
    public long getLength() {
        return file.length();
    }

    /**
     * append a new line to the log file, also output it to console
     *
     * @param obj the object to write to file
     */
    public void append(Object obj) throws IOException {
        append(obj.toString());
    }
}
