package com.ylm.lmpuffpicturebankend.manage.upload;

import cn.hutool.core.io.FileUtil;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Override
    protected void processSourceFile(Object uploadDataSources, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) uploadDataSources;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object uploadDataSources) {
        MultipartFile multipartFile = (MultipartFile) uploadDataSources;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void verifyTheImage(Object uploadDataSources) {
        MultipartFile multipartFile = (MultipartFile) uploadDataSources;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "参数为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        // 校验文件后缀
        String picSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件格式
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(picSuffix), ErrorCode.PARAMS_ERROR, "文件格式不正确");
    }
}
