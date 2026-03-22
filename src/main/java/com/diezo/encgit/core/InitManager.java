package com.diezo.encgit.core;

import com.diezo.encgit.RepositoryConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class InitManager {
    public static boolean init(Path repoRoot) throws IOException {
        Path encgitDir = repoRoot.resolve(".encgit");

        // Attempt to generate secure key
        String keyRef = EncryptionManager.generateKey();

        // Create .encgit directory
        try {
            Files.createDirectory(encgitDir);
        } catch (IOException e) {
            System.out.println("Existing Encgit repository found in " + repoRoot + "; Delete the .encgit directory and try again");
            return false;
        }

        resolveSubDirectories(encgitDir);
        resolveFiles(encgitDir, repoRoot, keyRef);

        return true;
    }

    private static void resolveSubDirectories(Path gitDir) throws IOException {
        Files.createDirectory(gitDir.resolve("objects"));  // objects
        Files.createDirectories(gitDir.resolve("refs").resolve("heads"));  // refs/heads
    }

    private static void resolveFiles(Path gitDir, Path repoRoot, String keyRef) throws IOException {
        // index.json
        StageManager.initialiseIndex(repoRoot);

        // ref.key
        Files.writeString(gitDir.resolve("ref.key"), keyRef + "\n");

        // HEAD
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/" + RepositoryConstants.DEFAULT_BRANCH_NAME + "\n");
    }
}
