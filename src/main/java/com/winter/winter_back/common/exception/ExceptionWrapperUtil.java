package com.winter.winter_back.common.exception;


import com.winter.winter_back.common.dto.BaseConstant;
import com.winter.winter_back.common.dto.ResponseBaseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class ExceptionWrapperUtil  { // 240103 hjkim 익셉션 처리를 감싸서 ResponseBaseDto으로 반환해주는 클래스

    @Autowired
    private Environment env;

    private static Map<String, Object> exceptionInfo;
    @PostConstruct
    public void setExceptionInfo(){
        YamlMapFactoryBean yaml = new YamlMapFactoryBean();
        String exceptionFile = env.getProperty("cnf.exception.path");
        if (StringUtils.hasText(exceptionFile)){
            yaml.setResources(new ClassPathResource(exceptionFile));
            exceptionInfo = yaml.getObject();
        }
    }
    public Map<String, Object> getExceptionInfo(String type) {
        Map<String, Object> info = null;
        if (exceptionInfo != null){
            info = (Map<String, Object>) exceptionInfo.get(type);
            if (info == null){
                info = getExceptionInfo("unknown");
            }
        }else {
            info = Collections.emptyMap();
        }
        return info;
    }

    public ResponseBaseDto<Object> makeResponse(String type) {
        return makeResponse(type, (String[]) null);
    }

    public ResponseBaseDto<Object> makeResponse(String type, String[] params){
        Map<String, Object> excepInfo = getExceptionInfo(type);
        log.info("[makeResponse] > [excepInfo] === {}", excepInfo);
        return new ResponseBaseDto<>(
                String.valueOf(excepInfo.get(BaseConstant.RES_CODE)),
                null != params && params.length > 0
                        ? String.valueOf(excepInfo.get(BaseConstant.RES_DESC)) + BaseConstant.STRING_BLANK + BaseConstant.STRING_HYPHEN + BaseConstant.STRING_BLANK + String.join(BaseConstant.STRING_COMMA, params)
                        : String.valueOf(excepInfo.get(BaseConstant.RES_DESC)),
                Integer.valueOf(String.valueOf(excepInfo.get(BaseConstant.RES_STATUS)))
        );
    }

    public ResponseBaseDto<Object> makeResponse(String type, Object[] params){
        Map<String, Object> excepInfo = getExceptionInfo(type);
        log.info("[makeResponse] > [excepInfo] === {}", excepInfo);
        return new ResponseBaseDto<>(
                String.valueOf(excepInfo.get(BaseConstant.RES_CODE)),
                MessageFormat.format(String.valueOf(excepInfo.get(BaseConstant.RES_DESC)), params),
                Integer.valueOf(String.valueOf(excepInfo.get(BaseConstant.RES_STATUS)))
        );
    }

    // List<String> 매개변수를 받는 오버로드 메서드 추가
    public ResponseBaseDto<Object> makeResponse(String type, List<String> params){
        if (params == null || params.isEmpty()) {
            return makeResponse(type, (String[]) null);
        }
        return makeResponse(type, params.toArray(new String[0]));
    }
}
