package org.isegodin.deeplearning.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.isegodin.deeplearning.data.dict.PetType;
import org.isegodin.deeplearning.data.response.ResponseWrapper;
import org.isegodin.deeplearning.service.PredictPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

/**
 * @author isegodin
 */
@Controller
@Slf4j
public class CommonController {

    @Autowired
    private PredictPetService predictPetService;

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

    @SneakyThrows
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseWrapper<PetType> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("File uploaded {}", file.getOriginalFilename());
        return ResponseWrapper.<PetType>builder().data(predictPetService.predictFromBytes(file.getBytes())).build();
    }

    @SneakyThrows
    @PostMapping(value = "/file/url", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseWrapper<PetType> uploadFile(@RequestBody String urlString) {
        log.info("URL uploaded {}", urlString);
        return ResponseWrapper.<PetType>builder().data(predictPetService.predictFromUrl(new URL(urlString))).build();
    }
}
