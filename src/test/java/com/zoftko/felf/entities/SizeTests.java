package com.zoftko.felf.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SizeTests {

    @Test
    void percentDiff() {
        assertThat(Size.percentDiff(100L, 200L)).isEqualTo("+100.00%");
        assertThat(Size.percentDiff(960L, 1080L)).isEqualTo("+12.50%");
        assertThat(Size.percentDiff(2048L, 1024L)).isEqualTo("-50.00%");
    }
}
