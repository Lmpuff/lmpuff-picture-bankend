package com.ylm.lmpuffpicturebankend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylm.lmpuffpicturebankend.annotation.AuthCheck;
import com.ylm.lmpuffpicturebankend.common.BaseResponse;
import com.ylm.lmpuffpicturebankend.common.DeleteRequest;
import com.ylm.lmpuffpicturebankend.common.ResultUtils;
import com.ylm.lmpuffpicturebankend.constant.UserConstant;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.exception.ThrowUtils;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureEditRequest;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureQueryRequest;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureUpdateRequest;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureUploadRequest;
import com.ylm.lmpuffpicturebankend.model.entity.Picture;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.ylm.lmpuffpicturebankend.model.vo.PictureTagCategory;
import com.ylm.lmpuffpicturebankend.model.vo.PictureVO;
import com.ylm.lmpuffpicturebankend.service.PictureService;
import com.ylm.lmpuffpicturebankend.service.UserService;
import com.ylm.lmpuffpicturebankend.service.impl.PictureServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);

    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long id = loginUser.getId();
        // 判断是否存在
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可以进行删除
        ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTH_ERROR);
        // 操作数据库
        boolean result = pictureService.removeById(picture.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片，仅管理员可用
     *
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO进行转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 将JSON转成String
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.verifyTheImage(picture);
        // 判断是否存在
        Long id = pictureUpdateRequest.getId();
        Picture pictureServiceById = pictureService.getById(id);
        ThrowUtils.throwIf(pictureServiceById == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 查找图片，仅管理员可用
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<Picture> getPictureById(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture pictureServiceById = pictureService.getById(id);
        ThrowUtils.throwIf(pictureServiceById == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pictureServiceById);
    }

    /**
     * 根据 id 获取封装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(PictureVO.objToVo(picture));
    }

    /**
     * 分页获取图片列表，仅管理员可用
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> page = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取图片列表，封装类
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 限制过多获取图片
        int pageSize = pictureQueryRequest.getPageSize();
        int current = pictureQueryRequest.getCurrent();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, httpServletRequest));
    }

    /**
     * 修改图片，供用户使用
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR);
        // 将实体类和DTO进行转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(picture, pictureEditRequest);
        // 将list 转为String
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.verifyTheImage(picture);
        // 判断是否存在
        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        // 仅本人或管理员可以进行修改
        ThrowUtils.throwIf(!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTH_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "二次元", "艺术", "校园", "背景", "创意");
        List<String> categoryList = Arrays.asList("二次元", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


}
