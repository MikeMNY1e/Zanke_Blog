package com.zanke.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Zanke
 * @version 1.0.0
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageVo<T> {

    private long total;
    private T rows;
}
