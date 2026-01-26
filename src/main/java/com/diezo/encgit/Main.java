package com.diezo.encgit;

import com.diezo.encgit.cli.CommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class Main
{
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new CommandParser()).execute(args);
        System.exit(exitCode);
    }
}
