package com.ylm.lmpuffpicturebankend.service;

import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureUploadRequest;
import com.ylm.lmpuffpicturebankend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.ylm.lmpuffpicturebankend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author MI
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-16 22:20:11
*/
public interface PictureService extends IService<Picture> {

    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                            User loginUser);

}
