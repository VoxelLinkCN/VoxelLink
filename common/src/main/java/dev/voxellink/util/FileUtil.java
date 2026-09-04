package dev.voxellink.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static void unzip(File zipFilePath, File destDirectory) throws IOException {
        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory.getAbsolutePath() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File file = new File(filePath);
        File destDir = file.getParentFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static void copyFolder(File source, File target) throws IOException {
        File[] files = source.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            File targetFile = new File(target, file.getName());
            if (file.isDirectory()) {
                if (!targetFile.exists()) {
                    if (!targetFile.mkdirs()) {
                        throw new IOException("Failed to create directory: " + targetFile.getAbsolutePath());
                    }
                }
                copyFolder(file, targetFile);
            } else {
                copyFileWithReplace(file, targetFile);
            }
        }
    }

    private static void copyFileWithReplace(File sourceFile, File targetFile) throws IOException {
        Files.copy(
                sourceFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.COPY_ATTRIBUTES
        );
    }
}
