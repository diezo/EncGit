package com.diezo.encgit.objects;

import com.diezo.encgit.core.StageManager;
import com.diezo.encgit.utils.HashUtil;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class TreeObject extends GitObject {

    public TreeObject(FilesystemDirectory rootDirectory) {
        super("tree", null, rootDirectory, null);
    }

    public String writeToObjectsDir(SecretKey secureKey) throws IOException {
        return recursiveCreateTree(rootDirectory, secureKey);
    }

    private String recursiveCreateTree(FilesystemDirectory rootDirectory, SecretKey secureKey) throws IOException {
        ByteArrayOutputStream treeContent = new ByteArrayOutputStream();

        // Iterate entries
        for (FilesystemEntry entry : rootDirectory.entries) {

            if (entry instanceof FilesystemFile file) {  // Found file
                // Add file entry to tree content
                treeContent.write(("100644 " + file.title).getBytes());
                treeContent.write(0);
                treeContent.write(file.objectHash.getBytes(StandardCharsets.UTF_8));
            } else {
                FilesystemDirectory directory = (FilesystemDirectory) entry;

                treeContent.write(("040000 " + directory.title).getBytes());
                treeContent.write(0);
                treeContent.write(recursiveCreateTree(directory, secureKey).getBytes(StandardCharsets.UTF_8));
            }
        }

        return writeTreeObject(treeContent.toByteArray(), secureKey);
    }

    public static void catTreeObject(byte[] content) {
        while (content.length > 0) {
            int i = 0;
            while (i < content.length && content[i] != 0) i++;

            String modeTitleHeader = new String(Arrays.copyOfRange(content, 0, i), StandardCharsets.UTF_8);
            String mode = modeTitleHeader.split(" ")[0];
            String title = modeTitleHeader.split(" ")[1];

            String objectHash = new String(Arrays.copyOfRange(content, i, i + 65),  StandardCharsets.UTF_8);

            // Pretty print
            System.out.println(mode + " " + (mode.equals("100644") ? "blob" : "tree") + " " + objectHash + " " + title);

            content = Arrays.copyOfRange(content, i + 65, content.length);
        }
    }
}
