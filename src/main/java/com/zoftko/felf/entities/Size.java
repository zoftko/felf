package com.zoftko.felf.entities;

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
        this.text = text;
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public Long getBss() {
        return bss;
    }

    public void setBss(Long bss) {
        this.bss = bss;
    }
}
