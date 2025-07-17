package com.ylm.lmpuffpicturebankend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片标签分类列表视图
 */
@Data
public class PictureTagCategory {

    /**
     * 分类别表
     */
    private List<String> categoryList;

    /**
     * 标签列表
     */
    private List<String> tagList;

}
