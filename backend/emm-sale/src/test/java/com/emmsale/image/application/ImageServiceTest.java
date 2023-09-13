package com.emmsale.image.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.emmsale.helper.ServiceIntegrationTestHelper;
import com.emmsale.image.exception.ImageException;
import com.emmsale.image.exception.ImageExceptionType;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ImageServiceTest extends ServiceIntegrationTestHelper {
  
  private ImageService imageService;
  private AmazonS3 mockingAmazonS3;
  
  @BeforeEach
  void setUp() {
    mockingAmazonS3 = mock(AmazonS3.class);
    imageService = new ImageService(mockingAmazonS3);
  }
  
  @Test
  @DisplayName("uploadImages(): 올바른 확장자를 갖는 파일을 입력받으면 정상적으로 파일을 S3에 업로드한다.")
  void uploadImages_success() {
    //given
    final List<MultipartFile> files = List.of(
        new MockMultipartFile("test", "test.png", "", new byte[]{}),
        new MockMultipartFile("test", "test.jpg", "", new byte[]{}),
        new MockMultipartFile("test", "test.jpeg", "", new byte[]{}));
    BDDMockito.given(mockingAmazonS3.putObject(any(PutObjectRequest.class)))
        .willReturn(new PutObjectResult());
    
    //when
    imageService.uploadImages(files);
    
    //then
    verify(mockingAmazonS3, times(3))
        .putObject(any(PutObjectRequest.class));
    
  }
  
  @Test
  @DisplayName("uploadImages(): 지원하지 않는 확장자를 갖는 파일을 입력받으면 예외를 반환한다.")
  void uploadImages_fail_extension() {
    //given
    final List<MultipartFile> files = List.of(
        new MockMultipartFile("test", "test.txt", "", new byte[]{}));
    
    //when
    final ThrowingCallable actual = () -> imageService.uploadImages(files);
    
    //then
    Assertions.assertThatThrownBy(actual)
        .isInstanceOf(ImageException.class)
        .hasMessage(ImageExceptionType.INVALID_FILE_FORMAT.errorMessage());
  }
  
  @Test
  @DisplayName("deleteImages(): 올바른 확장자를 갖는 파일을 입력받으면 정상적으로 파일을 S3에 업로드한다.")
  void deleteImages_success() {
    //given
    final List<String> fileNames = List.of("test1", "test2", "test3");
    
    //when
    imageService.deleteImages(fileNames);
    
    //then
    verify(mockingAmazonS3, times(3))
        .deleteObject(any(DeleteObjectRequest.class));
    
  }
}
