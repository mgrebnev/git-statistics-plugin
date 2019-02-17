package com.solightingstats.git.statistics.plugin;

import com.solightingstats.git.statistics.plugin.model.Contributor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.solightingstats.git.statistics.plugin.utils.DateUtils.getCurrentDateTime;

@Mojo(name = "time")
public class GitStatisticsMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectDirectory;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String now = getCurrentDateTime();

        getLog().info("#### Start executing time: " + now + " ####");
        getLog().info("-------- [Configuration] ----- ");
        getLog().info("| Project directory: " + projectDirectory.getAbsolutePath());
        getLog().info("| Mask: (value not set)");
        getLog().info("------------------------------\n");

        getLog().info("Parse repository...");
        getLog().info("Parsing repository \"" + projectDirectory.getAbsolutePath() + "\" SUCCESSFUL");

        try {
            Repository repository = Git.open(projectDirectory).getRepository();

            getLog().info("Current branch: " + repository.getBranch());

            Git git = new Git(repository);

            List<String> maskList = Arrays.asList(".java", ".xml");
            List<Path> paths = Files
                    .walk(projectDirectory.toPath())
                    .filter(
                            (path) ->
                                    path.toFile().isFile()
                                            && maskList.stream().anyMatch((mask) -> path.toString().contains(mask))
                    )
                    .map((path) -> projectDirectory.toPath().relativize(path))
                    .collect(Collectors.toList());

            getLog().info("Files count: " + paths.size());

            Map<String, Contributor> contributors = new HashMap<>();

            getLog().info("");
            getLog().info("#### Start collect statistics ####");

            Long filesCount = 0l;

            paths.forEach((path) -> {
                try {
                    //System.out.println(path);
                    System.out.println(git.status().addPath("src/main/java/ru/tn/school/account/controllers/RegistrationController.java").call().getIgnoredNotInIndex());

                    git
                            .log()
                            .addPath(path.toString())
                            .call()
                            .forEach((log) -> {
                                String currentAuthor = log.getAuthorIdent().getName();
                                String currentAuthorEmail = log.getAuthorIdent().getEmailAddress();
                                if (contributors.containsKey(currentAuthor)) {
                                    contributors.get(currentAuthor).addContribution();
                                } else {
                                    Contributor contributor = new Contributor();
                                    contributor.setEmail(currentAuthorEmail);
                                    contributor.addContribution();
                                    contributors.put(currentAuthor, contributor);
                                }
                            });
                } catch (Exception e) {

                }
            });

            getLog().info("#### End collect statistics   ####");
            getLog().info("");
            getLog().info("---------- [RESULTS] ----------");
            getLog().info("");

            // TODO pretty print (count (percent) + table 
            for (Map.Entry<String, Contributor> entry: contributors.entrySet()) {
                getLog().info(entry.getKey() + ", contributions: " + entry.getValue().getContributionsCount());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Parsing statistics FAILURE! Error message: " + e.getMessage());
        }
    }


}