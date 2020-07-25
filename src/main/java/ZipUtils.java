import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * ZIP工具包
 * </p>
 * <p>
 * 依赖：ant-1.7.1.jar
 * </p>
 * 
 * @author IceWee
 * @date 2012-5-26
 * @version 1.0
 */
public class ZipUtils {
    
    /**
     * 使用GBK编码可以避免压缩中文文件名乱码
     */
    private static final String CHINESE_CHARSET = "GBK";
    
    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;
    
    /**
     * <p>
     * 压缩文件
     * </p>
     * 
     * @param sourceFolder 压缩文件夹
     * @param zipFilePath 压缩文件输出路径
     * @throws Exception
     */
    public static void zip(String sourceFolder, String zipFilePath) throws Exception {
        OutputStream out = new FileOutputStream(zipFilePath);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ZipOutputStream zos = new ZipOutputStream(bos);
        // 解决中文文件名乱码
        zos.setEncoding(CHINESE_CHARSET);
        File file = new File(sourceFolder);
        String basePath = null;
        if (file.isDirectory()) {
            basePath = file.getPath();
        } else {
            basePath = file.getParent();
        }
        zipFile(file, basePath, zos);
        zos.closeEntry();
        zos.close();
        bos.close();
        out.close();
    }
    
    /**
     * <p>
     * 递归压缩文件
     * </p>
     * 
     * @param parentFile
     * @param basePath
     * @param zos
     * @throws Exception
     */
    private static void zipFile(File parentFile, String basePath, ZipOutputStream zos) throws Exception {
        File[] files = new File[0];
        if (parentFile.isDirectory()) {
            files = parentFile.listFiles();
        } else {
            files = new File[1];
            files[0] = parentFile;
        }
        String pathName;
        InputStream is;
        BufferedInputStream bis;
        byte[] cache = new byte[CACHE_SIZE];
        for (File file : files) {
            if (file.isDirectory()) {
                pathName = file.getPath().substring(basePath.length() + 1) + "/";
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {
                pathName = file.getPath().substring(basePath.length() + 1);
                is = new FileInputStream(file);
                bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));
                int nRead = 0;
                while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                    zos.write(cache, 0, nRead);
                }
                bis.close();
                is.close();
            }
        }
    }
    
    public static String getDirectoryName(String zipFilePath) throws Exception{
    	String dirName = "";
    	ZipFile zipFile = new ZipFile(zipFilePath, CHINESE_CHARSET);
    	Enumeration<?> emu = zipFile.getEntries();
    	ZipEntry entry;
    	while (emu.hasMoreElements()) {
            entry = (ZipEntry) emu.nextElement();
            if (entry.isDirectory()) {
            	String name = entry.getName();
                if(name.contains("/")){
                	dirName = name.substring(0, name.lastIndexOf("/"));
                	break;
                }
            }
            
    	}
    	return dirName;
    }
    
    /**
     * <p>
     * 解压压缩包
     * </p>
     * 
     * @param zipFilePath 压缩文件路径
     * @param destDir 压缩包释放目录
     * @throws Exception
     */
    public static void unZip(String zipFilePath, String destDir) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilePath, CHINESE_CHARSET);
        Enumeration<?> emu = zipFile.getEntries();
        BufferedInputStream bis;
        FileOutputStream fos;
        BufferedOutputStream bos;
        File file, parentFile;
        ZipEntry entry;
        byte[] cache = new byte[CACHE_SIZE];
        while (emu.hasMoreElements()) {
            entry = (ZipEntry) emu.nextElement();
            String name = entry.getName();
            if(name.contains("/")){
            	name = name.substring(name.lastIndexOf("/")+1);
            }
            if (entry.isDirectory()) {
                new File(destDir + File.separator + name).mkdirs();
                continue;
            }
            bis = new BufferedInputStream(zipFile.getInputStream(entry));
            file = new File(destDir + File.separator + name);
            parentFile = file.getParentFile();
            if (parentFile != null && (!parentFile.exists())) {
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, CACHE_SIZE);
            int nRead = 0;
            while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                fos.write(cache, 0, nRead);
            }
            bos.flush();
            bos.close();
            fos.close();
            bis.close();
        }
        zipFile.close();
    }
    /**
     * 解压ZIP文件到指定路径并以zip包名字命名的文件夹中
     * @param zipFile zip包文件(解压完后会删除压缩包)
     * @param parent 要解压到的路径
     * @return zip包一级目录中所有的文件或者文件夹
     */
    public static File[] unZip(File zipFile,String parent) {
        ZipInputStream zis = null;
        BufferedOutputStream bos = null;
        try { // 解压文件存放的文件夹为zip文件名称命名的文件夹内
            String folder = parent + File.separator
                    + FilenameUtils.getBaseName(zipFile.getName());
            Charset gbk = Charset.forName("GBK");
            zis = new ZipInputStream(new FileInputStream(zipFile), gbk);
            java.util.zip.ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                String target = folder + File.separator + entry.getName();
                File file = new File(target);
                if (entry.isDirectory()) { // 文件夹
                    file.mkdirs();
                } else { // 文件
                    if (!file.getParentFile().exists()) { // 创建文件父目录
                        file.getParentFile().mkdirs();
                    }
                    // 写入文件
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    int read = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                    bos.close();
                }
            }
            zis.closeEntry();
            // 返回解压后的文件夹位置
            return new File(folder).listFiles();
        } catch (Exception e) {
            return null;
        } finally {
            IOUtils.closeQuietly(zis);
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
