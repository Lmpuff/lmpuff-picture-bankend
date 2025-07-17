package com.ylm.lmpuffpicturebankend.manage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.ylm.lmpuffpicturebankend.common.ResultUtils;
import com.ylm.lmpuffpicturebankend.config.CosClientConfig;
import com.ylm.lmpuffpicturebankend.exception.BusinessException;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.exception.ThrowUtils;
import com.ylm.lmpuffpicturebankend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

@Service
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile multipartFile,
                                             String uploadPathPrefix) {
        // 校验图片
        verifyTheImage(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 文件上传的名称, 由后台自定义，不需要用户管理
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),
                uuid, FileUtil.getSuffix(originalFilename));
        // 文件上传的路径
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            String picFormat = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picHeight * 1.0 / picWidth, 2).doubleValue();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(picFormat);

            return uploadPictureResult;
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 删除临时文件
     *
     * @param file
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("文件删除失败 = {}", file.getAbsolutePath());
        }
    }

    private void verifyTheImage(MultipartFile multipartFile) {
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
