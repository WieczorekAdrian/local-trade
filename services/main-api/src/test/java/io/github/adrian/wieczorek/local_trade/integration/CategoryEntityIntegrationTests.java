package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.service.category.dto.CategoryDto;
import io.github.adrian.wieczorek.local_trade.service.image.dto.ImageDto;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryEntity;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.category.CategoryRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.testutils.CategoryUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
@EnableWebSecurity
public class CategoryEntityIntegrationTests extends AbstractIntegrationTest {
  @Autowired
  UsersRepository usersRepository;
  @Autowired
  CategoryRepository categoryRepository;
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;

  @Test
  @WithMockUser(username = "test@test.com")
  @Transactional
  public void getAllCategories_thenCategoriesAreReturned() throws Exception {
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    mockMvc.perform(get("/categories")).andExpect(status().isOk())
        .andExpect(jsonPath("$.categories[0].name").value("test"));
  }

  @Test
  @WithMockUser(username = "testadmin@test.com", roles = "ADMIN")
  @Transactional
  public void postCategory_thenCategoriesAreReturned() throws Exception {
    UsersEntity user = UserUtils.createUserRoleAdmin();
    usersRepository.save(user);
    CategoryDto categoryDto = CategoryUtils.createCategoryDto();

    mockMvc
        .perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(categoryDto)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Test category"))
        .andExpect(jsonPath("$.description").value("Category for testing"))
        .andExpect(jsonPath("$.parentCategory").value("Test parent category"));

  }

  @ParameterizedTest
  @CsvSource({"POST, /categories , 201", "PUT , /categories/{id}, 200",
      "DELETE, /categories/{id}, 204 "})
  @WithMockUser(username = "testadmin@test.com", roles = "ADMIN")
  @Transactional
  public void happyPathForPostingDeleteAndChangeCategory_thenCategoriesAreReturned(
      String httpMethod, String endpointTemplate, int expectedStatus) throws Exception {
    UsersEntity user = UserUtils.createUserRoleAdmin();
    usersRepository.save(user);
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);
    CategoryDto categoryDto = CategoryUtils.createCategoryDto();

    String finalEndpoint = endpointTemplate.replace("{id}", categoryEntity.getId().toString());
    ResultActions resultActions;

    String jsonDtoToCategory = objectMapper.writeValueAsString(categoryDto);

    switch (httpMethod.toUpperCase()) {
      case "POST":
        resultActions = mockMvc
            .perform(post(finalEndpoint).contentType(MediaType.APPLICATION_JSON)
                .content(jsonDtoToCategory))
            .andExpect(jsonPath("$.name").value("Test category"))
            .andExpect(jsonPath("$.description").value("Category for testing"))
            .andExpect(jsonPath("$.parentCategory").value("Test parent category"));
        break;

      case "PUT":
        resultActions = mockMvc
            .perform(put(finalEndpoint).contentType(MediaType.APPLICATION_JSON)
                .content(jsonDtoToCategory))
            .andExpect(jsonPath("$.name").value("Test category"))
            .andExpect(jsonPath("$.description").value("Category for testing"))
            .andExpect(jsonPath("$.parentCategory").value("Test parent category"));
        break;

      case "DELETE":
        resultActions = mockMvc.perform(delete(finalEndpoint));
        break;
      default:
        throw new Exception("Unsupported HTTP method: " + httpMethod);
    }
    resultActions.andExpect(status().is(expectedStatus));
  }

  @ParameterizedTest
  @CsvSource({"POST, /categories , 403", "PUT , /categories/{id}, 403",
      "DELETE, /categories/{id}, 403 "})
  @WithMockUser(username = "test@test.com", roles = "USER")
  @Transactional
  public void badPathForPostingDeleteAndChangeCategoryWithUnauthorizedUser_thenCategoriesAreNotReturned(
      String httpMethod, String endpointTemplate, int expectedStatus) throws Exception {
    UsersEntity user = UserUtils.createUserRoleUser();
    usersRepository.save(user);
    CategoryDto categoryDto = CategoryUtils.createCategoryDto();
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);
    String finalEndpoint = endpointTemplate.replace("{id}", categoryEntity.getId().toString());
    ResultActions resultActions;
    String jsonDtoToCategory = objectMapper.writeValueAsString(categoryDto);

    switch (httpMethod.toUpperCase()) {
      case "POST":
        resultActions = mockMvc.perform(
            post(finalEndpoint).contentType(MediaType.APPLICATION_JSON).content(jsonDtoToCategory));
        break;
      case "PUT":
        resultActions = mockMvc.perform(
            put(finalEndpoint).contentType(MediaType.APPLICATION_JSON).content(jsonDtoToCategory));
        break;
      case "DELETE":
        resultActions = mockMvc.perform(delete(finalEndpoint));
        break;
      default:
        throw new Exception("Unsupported HTTP method: " + httpMethod);
    }
    resultActions.andExpect(status().is(expectedStatus));
  }

  @ParameterizedTest
  @CsvSource({"POST, /categories , 400", "PUT , /categories/, 400", "DELETE, /categories/, 404 "})
  @WithMockUser(username = "test@test.com", roles = "ADMIN")
  @Transactional
  public void badPathWrongRequest_thenReturnBadRequest(String httpMethod, String endpointTemplate,
      int expectedStatus) throws Exception {
    ImageDto dto = new ImageDto(UUID.randomUUID(), "999", "999", 9999, "9999");
    CategoryEntity categoryEntity = CategoryUtils.createCategoryForIntegrationTests();
    categoryRepository.save(categoryEntity);

    ResultActions resultActions;
    String jsonDtoToCategory = objectMapper.writeValueAsString(dto);

    switch (httpMethod.toUpperCase()) {
      case "POST":
        resultActions = mockMvc.perform(post(endpointTemplate)
            .contentType(MediaType.APPLICATION_JSON).content(jsonDtoToCategory));
        break;
      case "PUT":
        resultActions = mockMvc.perform(put(endpointTemplate + categoryEntity.getId().toString())
            .contentType(MediaType.APPLICATION_JSON).content(jsonDtoToCategory));
        break;
      case "DELETE":
        resultActions = mockMvc.perform(delete(endpointTemplate + "43443"));
        break;
      default:
        throw new Exception("Unsupported HTTP method: " + httpMethod);
    }
    resultActions.andExpect(status().is(expectedStatus));
  }
}
