package com.selenium4.test;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ScreenshotUtils {

    public static void saveScreenshot(File file, String fileName) {
        try {
            System.out.println("Save screenshot at path: " + file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.copyFile(file,new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
