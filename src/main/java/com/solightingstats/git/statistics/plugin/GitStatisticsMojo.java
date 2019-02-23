package com.solightingstats.git.statistics.plugin;

import com.solightingstats.git.statistics.plugin.model.Contributor;
import com.solightingstats.git.statistics.plugin.utils.PrintoutUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.solightingstats.git.statistics.plugin.utils.DateUtils.getCurrentDateTime;
import static com.solightingstats.git.statistics.plugin.utils.PrintoutUtils.getFormattedColumn;

@Mojo(name = "show")
public class GitStatisticsMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectDirectory;
    
    @Parameter(defaultValue = ".java", required = true)
    private List<String> masks;
    
    @Override
    public void execute() throws MojoExecutionException {
        String now = getCurrentDateTime();
        
        getLog().info("");
        getLog().info("#### Start executing time: " + now + " ####");
        getLog().info("");
        getLog().info("-------- [Configuration] ----- ");
        getLog().info("| Project directory: " + projectDirectory.getAbsolutePath());
        getLog().info("| Mask: " + masks.stream().collect(Collectors.joining(", ")));
        getLog().info("------------------------------\n");

        getLog().info("Parse repository...");
        getLog().info("Parsing repository \"" + projectDirectory.getAbsolutePath() + "\" SUCCESSFUL");

        try {
            Repository repository = Git.open(projectDirectory).getRepository();

            getLog().info("Current branch: " + repository.getBranch());

            Git git = new Git(repository);

            List<Path> paths = Files
                    .walk(projectDirectory.toPath())
                    .filter(
                            (path) ->
                                    path.toFile().isFile()
                                            && masks.stream().anyMatch((mask) -> path.toString().contains(mask))
                    )
                    .map((path) -> projectDirectory.toPath().relativize(path))
                    .collect(Collectors.toList());

            Map<String, Contributor> contributors = new HashMap<>();

            getLog().info("");
            getLog().info("#### Start collect statistics ####");
            getLog().info("Parsing...");
            
            Integer countErrors = 0;
            
            MutableLong trackedFilesCount = new MutableLong(0L);

            final Function<Path, String> 
                    optimizedPathFunction =
                                    (path) -> path
                                                .toString()
                                                .replaceAll("\\\\", "/");

            for (Path path: paths) {
                try {
                    final String filePath = optimizedPathFunction.apply(path);
                    
                    Iterable<RevCommit> contributionsIterable = git.log().addPath(filePath).call();
                    if (contributionsIterable.iterator().hasNext()) {
                        git.log()
                            .addPath(filePath)
                            .call()
                            .forEach((log) -> {
                                String currentAuthor = log.getAuthorIdent().getName();
                                String currentAuthorEmail = log.getAuthorIdent().getEmailAddress();
                                if (contributors.containsKey(currentAuthor)) {
                                    contributors.get(currentAuthor).addContribution();
                                } else {
                                    Contributor contributor = new Contributor();
                                    contributor.setName(currentAuthor);
                                    contributor.setEmail(currentAuthorEmail);
                                    contributor.addContribution();
                                    contributors.put(currentAuthor, contributor);
                                }
                            });
                        trackedFilesCount.increment();
                    }

                } catch (Exception e) {
                    ++countErrors;
                }
            }

            getLog().info("#### End collect statistics   ####");
            getLog().info("");
            getLog().info("---------- [RESULTS] ----------");

            getLog().info("|Parsing files count: " + trackedFilesCount);
            getLog().info("|Errors count: " + countErrors);

            getLog().info("");
            
            final MutableInt maxLengthName = new MutableInt(0);
            final MutableInt maxContributionsLength = new MutableInt(0);
            final MutableInt countAllContributions = new MutableInt(0);
            
            contributors.forEach((key, value) -> {
                if (key.length() >= maxLengthName.getValue())
                    maxLengthName
                            .setValue(
                                    key.length()
                            );
                
                if (value.getContributionsCount().toString().length() >= maxContributionsLength.getValue()) 
                    maxContributionsLength
                            .setValue(
                                    value
                                            .getContributionsCount()
                                            .toString()
                                            .length()
                            );
                
                countAllContributions.add(value.getContributionsCount());
            });

            final DecimalFormat defaultPercentFormat = new DecimalFormat();
            defaultPercentFormat.setMaximumFractionDigits(2);
            
            contributors
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getValue)
                    .sorted()
                    .forEach((contributor) -> {
                        final String author = contributor.getName();
                        final Integer contributions = contributor.getContributionsCount();
                        final Float contributionsPercent = ((float) contributions / (float) countAllContributions.getValue()) * 100.0f;
                        
                        getLog().info(
                                getFormattedColumn(author, maxLengthName.getValue()) 
                              + " | "
                              + getFormattedColumn(contributions, maxContributionsLength.getValue())
                              + " | "
                              + defaultPercentFormat.format(contributionsPercent).replace(",", ".") + "%"
                        );
                    });
            
            getLog().info("-------------------------------");
            getLog().info("");
        } catch (Exception e) {
            throw new MojoExecutionException("Parsing statistics FAILURE! Error message: " + e.getMessage());
        }
    }


}
