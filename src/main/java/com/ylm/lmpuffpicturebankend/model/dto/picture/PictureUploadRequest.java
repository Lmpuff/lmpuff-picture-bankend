package com.ylm.lmpuffpicturebankend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 3433213462822093879L;

    /**
     * 图片id（用于修改）
     */
    private Long id;

    /**
     * 图片上传地址
     */
    private String fileUrl;

}
