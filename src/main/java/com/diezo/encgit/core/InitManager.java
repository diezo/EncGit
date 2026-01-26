package com.diezo.encgit.core;

import com.diezo.encgit.RepositoryConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class InitManager {
    public static boolean init(Path repoRoot) throws IOException {
        Path encgitDir = repoRoot.resolve(".encgit");

        // Create .encgit directory
        try {
            Files.createDirectory(encgitDir);
        } catch (IOException e) {
            System.out.println("Existing Encgit repository found in " + repoRoot + "; Delete the .encgit directory and try again");
            return false;
        }

        resolveSubDirectories(encgitDir);
        resolveFiles(encgitDir, repoRoot);

        return true;
    }

    private static void resolveSubDirectories(Path gitDir) throws IOException {
        Files.createDirectory(gitDir.resolve("objects"));  // objects
    }

    private static void resolveFiles(Path gitDir, Path repoRoot) throws IOException {
        // index.json
        StageManager.initialiseIndex(repoRoot);
    }
}
