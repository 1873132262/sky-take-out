package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "webp")));

    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 图片上传
     *
     * @param file 图片文件
     * @return 图片访问地址
     */
    @PostMapping("/upload")
    @ApiOperation("图片上传")
    public Result<String> upload(@RequestParam(value = "file", required = false) MultipartFile file) {
        log.info("图片上传，文件名：{}", file == null ? null : file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            throw new BaseException(MessageConstant.UPLOAD_FILE_EMPTY);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BaseException(MessageConstant.UPLOAD_FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)
                || !ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BaseException(MessageConstant.UPLOAD_FILE_TYPE_NOT_SUPPORTED);
        }

        try {
            String objectName = UUID.randomUUID().toString().replace("-", "")
                    + "." + extension.toLowerCase(Locale.ROOT);
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("读取上传文件失败，文件名：{}", originalFilename, e);
            throw new BaseException(MessageConstant.UPLOAD_FAILED, e);
        }

    }
}
