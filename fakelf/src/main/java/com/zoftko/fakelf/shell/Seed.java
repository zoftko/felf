package com.zoftko.fakelf.shell;

import com.zoftko.felf.db.dao.AnalysisRepository;
import com.zoftko.felf.db.dao.InstallationRepository;
import com.zoftko.felf.db.dao.ProjectRepository;
import com.zoftko.felf.db.entities.Analysis;
import com.zoftko.felf.db.entities.Installation;
import com.zoftko.felf.db.entities.Project;
import com.zoftko.felf.db.entities.Size;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.stereotype.Component;

@ShellComponent
@Command(command = "seed")
public class Seed {

    private final Faker faker = new Faker();

    private final InstallationRepository installationRepository;
    private final AnalysisRepository analysisRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public Seed(
        InstallationRepository installationRepository,
        AnalysisRepository analysisRepository,
        ProjectRepository projectRepository
    ) {
        this.installationRepository = installationRepository;
        this.analysisRepository = analysisRepository;
        this.projectRepository = projectRepository;
    }

    @Command(command = "installation", description = "Create a collection of installations")
    public String seedInstallation(int amount) {
        for (int i = 0; i < amount; i++) {
            var installation = new Installation();
            var accountId = faker.number().numberBetween(1, Integer.MAX_VALUE);

            installation.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
            installation.setTarget(accountId);
            installation.setSender(accountId);
            installation.setAccount(accountId);
            installation.setTargetType(faker.options().option("User", "Organization"));
            installation.setAccountLogin(faker.internet().username().replace(".", "-"));

            installationRepository.save(installation);
        }

        return String.format("%d installations created", amount);
    }

    @Command(command = "project", description = "Create a collection of projects.")
    public String seedProject(int amount) {
        var installations = installationRepository.findAll();
        for (int i = 0; i < amount; i++) {
            var project = new Project();

            project.setInstallation(faker.options().nextElement(installations));
            project.setFullName(String.format("%s/%s", faker.lorem().word(), faker.lorem().word()));
            project.setDefaultBranch(faker.options().option("main", "master", "dev", "stable"));
            project.setToken(faker.internet().password());
            project.setPrivate(faker.random().nextBoolean());

            projectRepository.save(project);
        }

        return String.format("%d projects created", amount);
    }

    @Command(command = "analysis", description = "Create a collection of analysis")
    public String seedAnalysis(int amount) {
        var projects = projectRepository.findAll();
        for (int i = 0; i < amount; i++) {
            var analysis = new Analysis();

            var size = new Size();
            size.setText(faker.random().nextLong(1000, 10_000_000));
            size.setData(faker.random().nextLong(64, 512_000));
            size.setBss(faker.random().nextLong(0, 256_000));

            analysis.setSize(size);
            analysis.setSha(faker.hashing().sha1());
            analysis.setProject(faker.options().nextElement(projects));
            analysis.setRef(String.format("%d/merge", faker.random().nextInt(1, 5000)));
            analysis.setComment(Analysis.CommentStatus.NOOP);
            analysis.setCreatedAt(faker.date().past(365 * 3, TimeUnit.DAYS).toLocalDateTime());

            analysisRepository.save(analysis);
        }

        return String.format("%d analysis created", amount);
    }
}
