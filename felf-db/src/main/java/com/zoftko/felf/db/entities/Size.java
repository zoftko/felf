package com.zoftko.felf.db.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Size {

    @Column(name = "text_size")
    private Long text;

    @Column(name = "data_size")
    private Long data;

    @Column(name = "bss_size")
    private Long bss;

    public Long getText() {
        return text;
    }

    public void setText(Long text) {
        if (text < 0) {
            throw new IllegalArgumentException("Text size can't be negative");
        }
        this.text = text;
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        if (text < 0) {
            throw new IllegalArgumentException("Data size can't be negative");
        }
        this.data = data;
    }

    public Long getBss() {
        return bss;
    }

    public void setBss(Long bss) {
        if (bss < 0) {
            throw new IllegalArgumentException("BSS size can't be negative");
        }
        this.bss = bss;
    }

    public static String percentDiff(Long initialSize, Long finalSize) {
        double diff = ((double) (finalSize - initialSize) / initialSize) * 100;
        return String.format("%+.2f%%", diff);
    }
}
