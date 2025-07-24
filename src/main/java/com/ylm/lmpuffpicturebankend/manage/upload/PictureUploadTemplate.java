package com.ylm.lmpuffpicturebankend.manage.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.ylm.lmpuffpicturebankend.config.CosClientConfig;
import com.ylm.lmpuffpicturebankend.exception.BusinessException;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.manage.CosManager;
import com.ylm.lmpuffpicturebankend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     * @param uploadDataSources
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object uploadDataSources,
                                             String uploadPathPrefix) {
        //  todo 校验图片
        verifyTheImage(uploadDataSources);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        // todo
        String originalFilename = getOriginalFilename(uploadDataSources);
        // 文件上传的名称, 由后台自定义，不需要用户管理
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),
                uuid, FileUtil.getSuffix(originalFilename));
        // 文件上传的路径
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(uploadPath, null);
            // todo 处理文件来源
            processSourceFile(uploadDataSources, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取压缩后得到的文件信息
                CIObject ciPictureObject = objectList.get(0);
                // 使缩略图默认为压缩后的图片信息
                CIObject thumbnailCioObject = ciPictureObject;
                if (objectList.size() > 1) {
                    // 获取缩略图后得到的文件信息
                    thumbnailCioObject = objectList.get(1);
                }
                // 封装压缩图的返回结果
                return getUploadPictureResult(originalFilename, ciPictureObject, thumbnailCioObject);
            }

            // 封装返回结果
            return getUploadPictureResult(imageInfo, uploadPath, originalFilename, file);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }



    /**
     * 处理输入源并生成临时文件
     *
     * @param uploadDataSources
     * @param file
     */
    protected abstract void processSourceFile(Object uploadDataSources, File file) throws Exception;

    /**
     * 获取输入源的原始文件名
     * @param uploadDataSources
     * @return
     */
    protected abstract String getOriginalFilename(Object uploadDataSources);

    /**
     * 校验输入源(文件名或 Url)
     * @param uploadDataSources
     */
    protected abstract void verifyTheImage(Object uploadDataSources);

    /**
     * 构造返回对象
     *
     * @param originalFilename   原式文件名
     * @param ciPictureObject    压缩后的对象
     * @param thumbnailCioObject
     * @return
     */
    private UploadPictureResult getUploadPictureResult(String originalFilename, CIObject ciPictureObject, CIObject thumbnailCioObject) {
        String picFormat = ciPictureObject.getFormat();
        int picWidth = ciPictureObject.getWidth();
        int picHeight = ciPictureObject.getHeight();
        double picScale = NumberUtil.round(picHeight * 1.0 / picWidth, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // 设置压缩后的原图 Url
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + ciPictureObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(ciPictureObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(picFormat);
        // 设置缩略图 Url
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCioObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 构造返回对象
     * @param imageInfo
     * @param uploadPath
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureResult getUploadPictureResult(ImageInfo imageInfo, String uploadPath, String originalFilename, File file) {
        String picFormat = imageInfo.getFormat();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picHeight * 1.0 / picWidth, 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.getName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(picFormat);
        return uploadPictureResult;
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

}
