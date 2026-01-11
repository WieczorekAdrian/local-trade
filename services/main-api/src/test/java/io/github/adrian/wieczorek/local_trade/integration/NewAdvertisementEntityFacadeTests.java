package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.advertisement.AdvertisementRepository;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.advertisement.dto.RequestAdvertisementDto;
import io.github.adrian.wieczorek.local_trade.testutils.AdUtils;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class NewAdvertisementEntityFacadeTests extends AbstractIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private AdvertisementRepository advertisementRepository;
  @Autowired
  private UsersRepository usersRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private S3Client s3Client;
  private static final String bucketName = "advertisements";

  @Container
  static final MinIOContainer minioContainer = new MinIOContainer("minio/minio:latest")
      .withUserName("minioadmin").withPassword("minioadmin").withExposedPorts(9000);

  @DynamicPropertySource
  static void overrideS3Properties(DynamicPropertyRegistry registry) {
    String minioEndpoint =
        "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(9000);

    registry.add("s3.endpoint", () -> minioEndpoint);
    registry.add("s3.accessKey", minioContainer::getUserName);
    registry.add("s3.secretKey", minioContainer::getPassword);
    registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
    registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
  }

  @Container
  static final RabbitMQContainer rabbitMQContainer =
      new RabbitMQContainer("rabbitmq:3.13-management");

  @BeforeEach
  public void cleanBucket() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
    } catch (NoSuchBucketException e) {
      s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
    }

    ListObjectsV2Request listRequest = ListObjectsV2Request.builder().bucket(bucketName).build();

    s3Client.listObjectsV2(listRequest).contents().forEach(obj -> s3Client
        .deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(obj.key()).build()));
  }

  @WithMockUser("test@test.com")
  @Test
  @Transactional
  public void testCreateWholeNewAdvertisement_thenReturnAdvertisementCreated() throws Exception {
    RequestAdvertisementDto baseAdvertisementDto = AdUtils.createRequestAdvertisementDto();
    List<MockMultipartFile> files = new ArrayList<>();
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.saveAndFlush(categoryEntity);

    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.saveAndFlush(user);

    RequestAdvertisementDto finalAdvertisementDto =
        baseAdvertisementDto.withCategoryId(categoryEntity.getId());

    String advertisementJson = objectMapper.writeValueAsString(finalAdvertisementDto);

    MockMultipartFile jsonPart = new MockMultipartFile("advertisementDto", "",
        MediaType.APPLICATION_JSON_VALUE, advertisementJson.getBytes());

    String listParameterName = "files";

    BufferedImage originalImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ImageIO.write(originalImage, "jpg", os);
    byte[] imageBytes = os.toByteArray();

    for (int i = 0; i < 5; i++) {
      MockMultipartFile file = new MockMultipartFile(listParameterName, "file" + i + ".jpg",
          MediaType.IMAGE_JPEG_VALUE, imageBytes);
      files.add(file);
    }

    MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/advertisements/new");

    requestBuilder.file(jsonPart);

    for (MockMultipartFile file : files) {
      requestBuilder.file(file);
    }

    mockMvc.perform(requestBuilder).andExpect(status().isOk())
        .andExpect(jsonPath("$.imageUrls").isArray()).andExpect(jsonPath("$.imageUrls", hasSize(5)))
        .andExpect(jsonPath("$.imageUrls[0]", isA(String.class)))
        .andExpect(jsonPath("$.imageUrls[0]", startsWith("http")))
        .andExpect(jsonPath("$.thumbnailUrls").isArray())
        .andExpect(jsonPath("$.thumbnailUrls", hasSize(5)))
        .andExpect(jsonPath("$.thumbnailUrls[0]", isA(String.class)))
        .andExpect(jsonPath("$.thumbnailUrls[0]", startsWith("http")));
  }
}
