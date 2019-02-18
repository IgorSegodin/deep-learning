package org.isegodin.deeplearning.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.isegodin.deeplearning.data.dict.PetType;
import org.isegodin.deeplearning.data.response.ResponseWrapper;
import org.isegodin.deeplearning.service.PredictPetService;
import org.isegodin.deeplearning.util.FileStreamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
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
    public ResponseWrapper<PetType> uploadFile(HttpServletRequest request) {
        ResponseWrapper.ResponseWrapperBuilder<PetType> responseBuilder = ResponseWrapper.<PetType>builder();

        FileStreamUtil.initDataProcessor(request)
                .fileConsumer("file", fileStreamApi -> responseBuilder.data(predictPetService.predictFromStream(fileStreamApi.getInputStream())))
                .process();

        return responseBuilder.build();
    }

    @SneakyThrows
    @PostMapping(value = "/file/url", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseWrapper<PetType> uploadUrl(@RequestBody String urlString) {
        return ResponseWrapper.<PetType>builder().data(predictPetService.predictFromUrl(new URL(urlString))).build();
    }
}
