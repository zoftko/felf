package com.zoftko.fakelf.shell;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(Seed.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeedTests {

    @Autowired
    Seed seed;

    @Test
    void testSeedAll() {
        assertThat(seed.seedInstallation(15)).isEqualTo("15 installations created");
        assertThat(seed.seedProject(30)).isEqualTo("30 projects created");
        assertThat(seed.seedAnalysis(50)).isEqualTo("50 analysis created");
    }
}
