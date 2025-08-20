package com.shop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Slf4j
@Service
public class FileService {

    /*
    <<args>>
    uploadPath : 파일을 저장할 위치(파일 시스템)
    originalFileName : 유저가 첨부한 파일의 원래 이름
    fileData : 파일의 바이너리 데이터(원본)

    <<purpose>>
    파일을 파일시스템에 저장
    originalFileName 그대로 저장하면 중복(덮어쓰기) 위험
    originalFileName ==> 중복 위험에 안전한(UUID를 적용한)이름으로 변경해서 저장
    */
    public String uploadFile(String uploadPath, String originalFileName,
                             byte[] fileData) throws Exception {
        /*
        originalFileName ==> UUID가 적용된 이름으로 변경
        1. UUID 생성
        2. originalFileName 에서 파일 확장자 얻기
        */
        UUID uuid = UUID.randomUUID();
        //te.st.png ==> .png
        //(test.png).substring(.이 있는 인덱스 숫자) ==> .png
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;
        log.info("[방금 저장된 파일명] : {}", savedFileName);
        //파일 시스템에 저장할 위치(fileUploadFullUrl)와 STREAM 연결
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        //연결된 STREAM 통해 데이터를 저장
        fos.write(fileData);
        fos.close();
        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception {
        File deleteFile = new File(filePath);
        if(deleteFile.exists()) {
            deleteFile.delete();
            log.info("[파일 삭제] : {}", filePath);
        }else {
            log.info("[존재하지 않는 파일] : {}", filePath);
        }

    }

}
