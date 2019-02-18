package org.isegodin.deeplearning.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.isegodin.deeplearning.data.response.ResponseWrapper;
import org.isegodin.deeplearning.exception.RestException;
import org.isegodin.deeplearning.service.FileService;
import org.isegodin.deeplearning.service.PredictPetService;
import org.isegodin.deeplearning.util.FileStreamUtil;
import org.isegodin.deeplearning.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;

/**
 * @author isegodin
 */
@Controller
@Slf4j
public class AdminController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PredictPetService predictPetService;

    @Value("${deep-learning.admin.password}")
    private String adminPassword;

    @GetMapping("/admin")
    public String getAdminPage() {
        return "admin";
    }

    @SneakyThrows
    @PostMapping(value = "/admin/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseWrapper<String> uploadModel(@RequestHeader String access, HttpServletRequest request) {
        if (adminPassword == null || adminPassword.trim().isEmpty() || !HashUtil.sha256(adminPassword).equals(access)) {
            throw RestException.builder().message("Wrong or empty access key").httpStatus(HttpStatus.BAD_REQUEST).build();
        }
        try {
            FileStreamUtil.initDataProcessor(request).fileConsumer("file", fileStreamApi -> {
                fileService.addFile("model.zip", fileStreamApi.getInputStream());
            }).process();

            predictPetService.updateModel(new FileInputStream(fileService.getFile("model.zip")));
        } catch (Exception e) {
            throw RestException.builder().message("Failed to update").httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseWrapper.<String>builder().data("OK").build();
    }
}
