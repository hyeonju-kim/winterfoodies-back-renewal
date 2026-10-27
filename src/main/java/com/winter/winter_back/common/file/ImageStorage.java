package com.winter.winter_back.common.file;

import com.winter.winter_back.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * description    : 이미지 파일 저장소 (로컬 폴더). 배포 시 클라우드 저장소로 바꿀 때 이 클래스만 교체한다
 * fileName       : ImageStorage
 * author         : hj kim
 * date           : 26. 10. 26.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 10. 26.       hj kim             최초 생성
 */
@Slf4j
@Component
public class ImageStorage {

    /** 앱에 내려주는 사진 주소 앞부분 (WebConfig 가 이 경로를 저장 폴더로 연결) */
    public static final String URL_PREFIX = "/uploads/";

    private static final int HEADER_SIZE = 12;

    private final Path rootDirectory;

    public ImageStorage(@Value("${app.upload.dir}") String uploadDirectory) throws IOException {
        this.rootDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(rootDirectory);
        log.info("[ImageStorage] 사진 저장 폴더: {}", rootDirectory);
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    /**
     * Author      : hj kim
     * Description : 사진 저장. 파일 앞부분(매직 넘버)으로 jpg·png·webp 인지 확인하고, 이름은 새로 만든다(사용자 파일 이름은 쓰지 않음).
     *               돌려주는 값은 서버 주소를 뺀 경로(/uploads/폴더/파일) - 서버 주소가 바뀌어도 DB 를 고칠 필요가 없다.
     *               트랜잭션 안이면 롤백될 때 저장한 파일을 지운다
     * Date        : 26. 10. 26.
     */
    public String saveImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BizException("file_empty");
        }
        String extension = getImageExtension(file);
        Path directory = rootDirectory.resolve(folder).normalize();
        if (!directory.startsWith(rootDirectory)) {
            throw new BizException("invalid_image_format");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, directory.resolve(fileName));
            }
        } catch (IOException exception) {
            log.error("[saveImage] 저장 실패: {}/{}", directory, fileName, exception);
            throw new BizException("fileUpload.ConvertFail");
        }
        String url = URL_PREFIX + rootDirectory.relativize(directory.resolve(fileName)).toString().replace('\\', '/');
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        deleteNow(url);
                    }
                }
            });
        }
        return url;
    }

    /**
     * 사진 삭제. 트랜잭션 안이면 커밋된 뒤에 지운다 (DB 가 롤백되면 예전 사진이 남아 있어야 하므로).
     * 이 저장소가 만든 경로(/uploads/...)가 아니면(테스트 데이터의 외부 주소 등) 아무것도 하지 않는다
     */
    public void deleteImage(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteNow(url);
                }
            });
        } else {
            deleteNow(url);
        }
    }

    private void deleteNow(String url) {
        Path path = rootDirectory.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!path.startsWith(rootDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("[deleteImage] 삭제 실패: {}", path, exception);
        }
    }

    // 파일 앞부분으로 형식 판단: jpg(FF D8 FF) / png(89 50 4E 47) / webp(RIFF....WEBP)
    private String getImageExtension(MultipartFile file) {
        byte[] header = new byte[HEADER_SIZE];
        int length;
        try (InputStream input = file.getInputStream()) {
            length = input.readNBytes(header, 0, HEADER_SIZE);
        } catch (IOException exception) {
            throw new BizException("invalid_image_format");
        }
        if (length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (length >= 4 && (header[0] & 0xFF) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
            return "png";
        }
        if (length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return "webp";
        }
        throw new BizException("invalid_image_format");
    }
}
