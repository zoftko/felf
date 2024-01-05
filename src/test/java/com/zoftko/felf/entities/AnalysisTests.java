package com.zoftko.felf.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AnalysisTests {

    @Test
    void testPullRequestAnalysis() {
        var analysis = new Analysis();
        analysis.setRef("456/merge");

        assertThat(analysis.getIssueId()).contains("456");

        analysis.setRef("7/merge");
        assertThat(analysis.getIssueId()).contains("7");
    }

    @Test
    void testBranchAnalysis() {
        var analysis = new Analysis();
        analysis.setRef("main");

        assertThat(analysis.getIssueId()).isEmpty();
    }
}
