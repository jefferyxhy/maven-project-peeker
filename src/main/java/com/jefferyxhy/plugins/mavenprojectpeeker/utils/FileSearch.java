package com.jefferyxhy.plugins.mavenprojectpeeker.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSearch {
    private String fileName = "";
    private List<String> result = new ArrayList<>();

    public List<String> searchDirectory(File directory, String fileName) {
        this.fileName = fileName;

        if (directory.isDirectory()) {
            search(directory);
        } else {
            System.out.println(directory.getAbsoluteFile() + " is not a directory!");
        }

        return result;
    }

    private void search(File file) {
        if(!file.isDirectory()) {
            return;
        }

        if(!file.canRead()) {
            System.out.println("No read permission for " + file.getAbsoluteFile() );
            return;
        }

        Arrays.stream(file.listFiles()).forEach(tmpFile -> {
            if (tmpFile.isDirectory()) {
                search(tmpFile);
            } else {
                if (fileName.equals(tmpFile.getName().toLowerCase())) {
                    result.add(tmpFile.getAbsoluteFile().toString());
                }
            }
        });
    }
}
