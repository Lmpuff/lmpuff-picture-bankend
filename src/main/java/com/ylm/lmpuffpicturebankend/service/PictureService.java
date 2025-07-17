package com.ylm.lmpuffpicturebankend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureQueryRequest;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureUploadRequest;
import com.ylm.lmpuffpicturebankend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.ylm.lmpuffpicturebankend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author MI
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-16 22:20:11
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取查询包装类
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片封装类
     *
     * @param picture
     * @param httpServletRequest
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest httpServletRequest);

    /**
     * 获取图片分页封装类
     *
     * @param picturePage
     * @param httpServletRequest
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest httpServletRequest);

    void verifyTheImage(Picture picture);

}
