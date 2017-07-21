package com.oversea.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author fengjian
 * @version V1.0
 * @title: sea-online
 * @Package com.task.tfb.client
 * @Description: zip解压缩
 * @date 15/12/5 22:27
 */
public class ZipUtil {
    /**
     * 解压到指定目录
     *
     * @param zipPath
     * @param descDir
     */
//    public static void unZipFiles(String zipPath, String descDir) throws IOException {
//        unZipFiles(new File(zipPath), descDir);
//    }

    /**
     * 解压文件到指定目录
     */
//    public static void unZipFiles(File zipFile, String descDir) throws IOException {
//        File pathFile = new File(descDir);
//        if (!pathFile.exists()) {
//            pathFile.mkdirs();
//        }
//        ZipFile zip = new ZipFile(zipFile);
//        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
//            ZipEntry entry = (ZipEntry) entries.nextElement();
//            String zipEntryName = entry.getName();
//            InputStream in = zip.getInputStream(entry);
//            String outPath = (descDir + zipEntryName);
//            File file = new File(outPath.substring(0, outPath.lastIndexOf('\\')));
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            if (new File(outPath).isDirectory()) {
//                continue;
//            }
//            System.out.println(outPath);
//            OutputStream out = new FileOutputStream(outPath);
//            byte[] buf1 = new byte[1024];
//            int len;
//            while ((len = in.read(buf1)) > 0) {
//                out.write(buf1, 0, len);
//            }
//            in.close();
//            out.close();
//        }
//        zip.close();
//        System.out.println("******************解压完毕********************");
//    }

    public static void unZipFiles(final String fileName, final String filePath) {
        try {
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration<?> emu = zipFile.entries();
            int BUFFER = 4096;
            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();
                // 会把目录作为一个file读出一次，所以只建立目录就可以，之下的文件还会被迭代到。
                if (entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(filePath + entry.getName());
                // 加入这个的原因是zipfile读取文件是随机读取的，这就造成可能先读取一个文件
                // 而这个文件所在的目录还没有出现过，所以要建出目录来。
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                bis.close();
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String outPath = "C:\\auto\\lib\\selenium-remote-driver-2.48.2.jar";
        System.out.println(outPath.substring(0, outPath.lastIndexOf('\\')));
    }
}
