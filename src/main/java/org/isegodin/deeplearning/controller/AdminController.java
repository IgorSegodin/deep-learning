package org.isegodin.deeplearning.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.isegodin.deeplearning.data.response.ResponseWrapper;
import org.isegodin.deeplearning.service.PredictPetService;
import org.isegodin.deeplearning.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author isegodin
 */
@Controller
@Slf4j
public class AdminController {

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
    public ResponseWrapper<String> uploadFile(@RequestHeader String access, @RequestParam("file") MultipartFile file) {
        if(adminPassword == null || adminPassword.trim().isEmpty() || !HashUtil.sha256(adminPassword).equals(access)) {
            return ResponseWrapper.<String>builder().data("ERROR").build();
        }
        try {
            predictPetService.updateModel(file.getBytes());
        } catch (IOException e) {
            log.warn("Model update fail", e);
            return ResponseWrapper.<String>builder().data("ERROR").build();
        }
        return ResponseWrapper.<String>builder().data("OK").build();
    }
}
