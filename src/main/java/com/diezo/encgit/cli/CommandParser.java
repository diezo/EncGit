package com.diezo.encgit.cli;

import com.diezo.encgit.core.EncryptionManager;
import com.diezo.encgit.core.StageManager;
import com.diezo.encgit.core.CommitManager;
import com.diezo.encgit.core.InitManager;
import com.diezo.encgit.objects.GitObject;
import picocli.CommandLine;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "encgit",
        header = "encgit - an encrypted Git implementation",
        subcommands = {
                InitCmd.class,
                AddCmd.class,
                CommitCmd.class,
                CatFileCmd.class
        },
        mixinStandardHelpOptions = true
)
public class CommandParser implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }

    public static boolean isEncgitRepo() {
        Path repoRoot = Paths.get("").toAbsolutePath();
        boolean repoExists = Files.isDirectory(repoRoot.resolve(".encgit"));

        if (!repoExists) {
            System.out.println("No Encgit repository found in " + repoRoot);
        }

        return repoExists;
    }
}

@CommandLine.Command(
        name = "init",
        description = "Initialize Encgit repository"
)
class InitCmd implements Runnable {

    @Override
    public void run() {
        Path repoRoot = Paths.get("").toAbsolutePath();

        try {
            if (InitManager.init(repoRoot)) {
                System.out.println("Initialized empty Encgit Repository in " + repoRoot);
            }
        }
        catch (IOException e) { System.out.println("Error: Could not initialize repo"); }
    }
}

@CommandLine.Command(
        name = "add",
        description = "Add files to staging area"
)
class AddCmd implements Runnable {

    @CommandLine.Parameters(
            arity = "0..*",
            description = "Files to stage"
    )
    List<String> files;

    @Override
    public void run() {
        if (!CommandParser.isEncgitRepo()) return;  // Requires encgit repo
        if (files == null || files.isEmpty()) {  // No file specified
            System.out.println("You didn't specify any files to stage");
            return;
        }

        Path repoRoot = Paths.get("").toAbsolutePath();

        List<Path> filePaths = new ArrayList<>();

        for (String file : files) {
            if (!Files.isRegularFile(Paths.get(file))) {
                System.out.println("File not found: " + file);
                continue;
            }

            filePaths.add(repoRoot.resolve(file).normalize());
        }

        StageManager.stageCommand(filePaths, repoRoot);
    }
}

@CommandLine.Command(
        name = "commit",
        description = "Commit files from the staging area"
)
class CommitCmd implements Runnable {

    @CommandLine.Option(
            names = {"-m", "--message"},
            description = "Commit message",
            required = true
    )
    String message;

    @Override
    public void run() {
        if (!CommandParser.isEncgitRepo()) return;  // Requires encgit repo
        if (this.message == null || this.message.isEmpty() || this.message.trim().isEmpty()) {
            System.out.println("Missing commit message. Please provide one using -m");
            return;
        }

        CommitManager.commitCommand(this.message.trim());
    }
}

@CommandLine.Command(
        name = "cat-file",
        description = "Inspect the specified object file"
)
class CatFileCmd implements Runnable {

    @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
    RequiredOption required;

    static class RequiredOption {

        @CommandLine.Option(
                names = {"-p"},
                description = "Print content of object"
        )
        String printBody;

        @CommandLine.Option(
                names = {"-t"},
                description = "Print type of object"
        )
        String printType;
    }

    @Override
    public void run() {
        if (!CommandParser.isEncgitRepo()) return;  // Requires encgit repo

        Path repoRoot = Paths.get("").toAbsolutePath();
        Path encgitDir = repoRoot.resolve(".encgit");

        // Attempt to load secure key
        SecretKey secureKey = EncryptionManager.loadSecureKey(encgitDir);
        
        GitObject.catObject(
                required.printBody != null ? required.printBody : required.printType,
                required.printBody != null ? GitObject.CAT_CONTENT : GitObject.CAT_TYPE,
                secureKey
        );
    }
}
