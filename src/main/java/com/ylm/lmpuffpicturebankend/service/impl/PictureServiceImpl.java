package com.ylm.lmpuffpicturebankend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ylm.lmpuffpicturebankend.exception.ErrorCode;
import com.ylm.lmpuffpicturebankend.exception.ThrowUtils;
import com.ylm.lmpuffpicturebankend.manage.FileManager;
import com.ylm.lmpuffpicturebankend.model.dto.file.UploadPictureResult;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureQueryRequest;
import com.ylm.lmpuffpicturebankend.model.dto.picture.PictureUploadRequest;
import com.ylm.lmpuffpicturebankend.model.entity.Picture;
import com.ylm.lmpuffpicturebankend.model.entity.User;
import com.ylm.lmpuffpicturebankend.model.vo.PictureVO;
import com.ylm.lmpuffpicturebankend.model.vo.UserVO;
import com.ylm.lmpuffpicturebankend.service.PictureService;
import com.ylm.lmpuffpicturebankend.mapper.PictureMapper;
import com.ylm.lmpuffpicturebankend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author MI
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-07-16 22:20:11
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile,
                                   PictureUploadRequest pictureUploadRequest,
                                   User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是上传还是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新，判断图片是否存在
        if (pictureId != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片，得到图片信息
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        String url = uploadPictureResult.getUrl();
        String picName = uploadPictureResult.getPicName();
        Long picSize = uploadPictureResult.getPicSize();
        int picWidth = uploadPictureResult.getPicWidth();
        int picHeight = uploadPictureResult.getPicHeight();
        Double picScale = uploadPictureResult.getPicScale();
        String picFormat = uploadPictureResult.getPicFormat();
        picture.setUrl(url);
        picture.setName(picName);
        picture.setPicSize(picSize);
        picture.setPicWidth(picWidth);
        picture.setPicHeight(picHeight);
        picture.setPicScale(picScale);
        picture.setPicFormat(picFormat);
        picture.setUserId(loginUser.getId());

        // 操作数据库
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        String searchText = pictureQueryRequest.getSearchText();
        // 从多字段中查询
        if (StrUtil.isNotBlank(searchText)) {
            // 拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText))
                    .or()
                    .like("introduction", searchText);
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // tags数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取脱敏后的图片信息
     *
     * @param picture
     * @param httpServletRequest
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest httpServletRequest) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 获取脱敏后的图片列表
     *
     * @param picturePage
     * @param httpServletRequest
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage,
                                            HttpServletRequest httpServletRequest) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(),
                picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        // 1. 构建 PictureVO 列表
        List<PictureVO> pictureVOList = new ArrayList<>();
        Set<Long> userIdSet = new HashSet<>();

        for (Picture picture : pictureList) {
            PictureVO pictureVO = PictureVO.objToVo(picture);
            Long userId = picture.getUserId();
            pictureVOList.add(pictureVO);
            userIdSet.add(userId);
        }

        // 2. 批量查询用户列表并构建 Map
        Map<Long, User> userIdUserMap = new HashMap<>();
        List<User> userList = userService.listByIds(userIdSet);

        for (User user : userList) {
            userIdUserMap.put(user.getId(), user);
        }

        // 3. 为每个 PictureVO 设置 UserVO
        for (PictureVO pictureVO : pictureVOList) {
            Long userId = pictureVO.getUserId();
            User user = userIdUserMap.get(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }

        // 4. 设置分页结果
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;

    }

    @Override
    public void verifyTheImage(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String pictureUrl = picture.getUrl();
        String pictureIntroduction = picture.getIntroduction();
        // 校验参数
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, " id 不能为空");
        if (StrUtil.isNotBlank(pictureUrl)) {
            ThrowUtils.throwIf(pictureUrl.length() > 1024, ErrorCode.PARAMS_ERROR, " url 过长");
        }
        if (StrUtil.isNotBlank(pictureIntroduction)) {
            ThrowUtils.throwIf(pictureIntroduction.length() > 512, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }
}




