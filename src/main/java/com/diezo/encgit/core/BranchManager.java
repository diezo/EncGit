package com.diezo.encgit.core;

import com.diezo.encgit.RepositoryConstants;
import com.diezo.encgit.objects.BlobObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.diezo.encgit.RepositoryConstants.NULL_SHA256_HASH;

public class BranchManager {
    private static final Logger log = LoggerFactory.getLogger(BranchManager.class);
    private static final Path repoRoot = Paths.get("").toAbsolutePath();

    public static void moveActiveBranchPointer(String commitHash) {
        try {
            // Update commit hash for active branch
            updateCommitHash(getActiveBranchName(), commitHash);
        } catch (IOException e) {
            System.out.println("Error: Unable to read HEAD file; Repository seems to be corrupted");
        }
    }

    private static String getActiveBranchName() throws IOException {
        String headFileContent = readHeadFile();

        // HEAD file content not parsable
        if (!headFileContent.startsWith("ref: refs/heads/")) throw new IOException();

        String activeBranchName = headFileContent.substring("ref: refs/heads/".length());

        // No active branch specified in HEAD file
        if (activeBranchName.isEmpty()) throw new IOException();

        return activeBranchName;
    }

    private static void updateCommitHash(String branchName, String commitHash) throws IOException {
        Files.writeString(repoRoot
                    .resolve(".encgit")
                    .resolve("refs")
                    .resolve("heads")
                    .resolve(branchName), commitHash);
    }

    public static String getParentCommitHash() throws IOException {
        return getBranchCommitHash(getActiveBranchName());
    }

    public static String getBranchCommitHash(String branchName) throws IOException {
        Path branchPath = repoRoot
                .resolve(".encgit")
                .resolve("refs")
                .resolve("heads")
                .resolve(branchName);

        // Branch file doesn't exist
        if (!Files.isRegularFile(branchPath)) return NULL_SHA256_HASH;

        String branchFileContent = Files.readString(branchPath, StandardCharsets.UTF_8).trim();

        // Branch file empty
        if (branchFileContent.isEmpty()) return NULL_SHA256_HASH;

        // Return possible branch commit hash
        return branchFileContent;
    }

    private static String readHeadFile() throws IOException {
        return Files.readString(repoRoot.resolve(".encgit").resolve("HEAD")).trim();
    }
}
